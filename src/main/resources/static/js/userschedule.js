document.addEventListener('DOMContentLoaded', function () {
    var mainCalendarEl = document.getElementById('calendar');
    var miniCalendarEl = document.getElementById('mini-calendar');
    var miniCalendarWrapper = document.getElementById('mini-calendar-wrapper');
    var monthPlaceholder = document.getElementById('month-placeholder');
    var ptButton = document.getElementById('pt-button');
    var ptInfo = document.getElementById('pt-info');
    var scheduleForm = document.getElementById('schedule-form'); // 일정 선택 폼 영역
    var startDateInput = document.getElementById('start-date');
    var chatBox = document.getElementById('chat-box'); // 채팅박스 영역
	
    // 사용자 일정 선택 모드 관련 변수
    var userSelectionMode = false;
    var userCreatedEvents = [];

    // 로컬 시간 문자열 포맷 함수 (ISO 형식, 초까지)
    function formatLocalDateTime(date) {
        var yyyy = date.getFullYear();
        var MM = ("0" + (date.getMonth() + 1)).slice(-2);
        var dd = ("0" + date.getDate()).slice(-2);
        var hh = ("0" + date.getHours()).slice(-2);
        var mm = ("0" + date.getMinutes()).slice(-2);
        var ss = ("0" + date.getSeconds()).slice(-2);
        return yyyy + "-" + MM + "-" + dd + "T" + hh + ":" + mm + ":" + ss;
    }

    var miniCalendar = new FullCalendar.Calendar(miniCalendarEl, {
        initialView: 'dayGridMonth',
        selectable: true,
        height: '300px',
        headerToolbar: false,
        dateClick: function(info) {
            mainCalendar.changeView('timeGridWeek', info.dateStr);
        }
    });

    var mainCalendar = new FullCalendar.Calendar(mainCalendarEl, {
        eventOverlap: true,
        customButtons: {
            logout: {
                text: '로그아웃',
                click: function () { alert('로그아웃 버튼 클릭됨'); }
            },
            personalInfo: {
                text: '개인정보',
                click: function () { alert('개인정보 버튼 클릭됨'); }
            }
        },
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'logout personalInfo dayGridMonth,timeGridWeek,timeGridDay'
        },
        initialDate: new Date(),
        initialView: 'dayGridMonth',
        navLinks: true,
        selectable: true,
        selectMirror: true,
        editable: true,
        dayMaxEvents: true,
        eventColor: '#3788d8', // 기본 DB 이벤트: 파란색
        eventOrder: function(a, b) {
            var aFlag = a.extendedProps.isUserCreated ? 1 : 0;
            var bFlag = b.extendedProps.isUserCreated ? 1 : 0;
            return bFlag - aFlag;
        },
        dateClick: function(info) {
            if (mainCalendar.view.type === 'dayGridMonth') {
                mainCalendar.changeView('timeGridWeek', info.dateStr);
            }
            // week/day view에서는 일정 생성은 폼 입력을 통해 진행하므로 아무 동작도 하지 않음.
        },
        select: function(info) {
            mainCalendar.unselect();
        },
        eventClick: function(info) {
            // 오직 사용자 생성 이벤트만 삭제 (기본 DB 이벤트는 삭제 불가)
            if ((mainCalendar.view.type === 'timeGridWeek' || mainCalendar.view.type === 'timeGridDay') && info.event.extendedProps.isUserCreated) {
                if (confirm('이 이벤트를 삭제하시겠습니까?')) {
                    var eventData = {
                        title: info.event.title,
                        start: info.event.startStr,
                        end: info.event.endStr,
                        allDay: info.event.allDay
                    };
                    deleteEventFromServer(eventData);
                    info.event.remove();
                }
            }
        },
        eventDidMount: function(info) {
            if (info.event.extendedProps.isUserCreated) {
                info.el.classList.add('user-created-event');
                info.el.style.cssText += "color: black !important; z-index: 9999 !important; position: relative !important;";
                var titleEl = info.el.querySelector('.fc-event-title');
                if (titleEl) {
                    titleEl.style.cssText += "color: black !important;";
                }
                var timeEl = info.el.querySelector('.fc-event-time');
                if (timeEl) {
                    timeEl.style.cssText += "color: black !important;";
                }
            } else {
                info.el.style.zIndex = '1';
            }
        },
        eventDrop: function(info) {
            if (!userSelectionMode) {
                alert('드래그 기능이 비활성화되어 있습니다.');
                info.revert();
                return;
            }
            if (!info.event.extendedProps.isUserCreated) {
                alert('기본 이벤트는 이동할 수 없습니다.');
                info.revert();
                return;
            }
        },
        eventResize: function(info) {
            if (!userSelectionMode) {
                alert('리사이즈 기능이 비활성화되어 있습니다.');
                info.revert();
                return;
            }
            if (!info.event.extendedProps.isUserCreated) {
                alert('기본 이벤트는 리사이즈할 수 없습니다.');
                info.revert();
                return;
            }
        },
        allDaySlot: false,
        views: {
            timeGridWeek: { allDaySlot: false, selectable: false },
            timeGridDay: { allDaySlot: false, selectable: false }
        },
        events: calendarEvents,
		datesSet: function(info) {
		    var viewType = info.view.type;
		    if (viewType === 'dayGridMonth') {
		        // 월간 뷰: 미니 캘린더, 일정 선택 버튼, 일정 입력 폼, 채팅박스는 숨기고,
		        // 잔여 PT(ptInfo)는 보이도록 설정
		        miniCalendarWrapper.style.display = 'none';
		        monthPlaceholder.style.display = 'none';
		        ptButton.style.display = 'none';
		        ptInfo.style.display = 'block';
		        scheduleForm.style.display = 'none';
		        chatBox.style.display = 'none';
		    } else if (viewType === 'timeGridWeek' || viewType === 'timeGridDay') {
		        // 주간/일간 뷰: 미니 캘린더와 일정 선택 버튼은 보이도록,
		        // 일정 입력 폼은 사용자가 일정 선택 모드일 때만 보이고,
		        // 잔여 PT는 계속 보이도록 설정
		        miniCalendarWrapper.style.display = 'block';
		        monthPlaceholder.style.display = 'none';
		        ptButton.style.display = 'block';
		        ptInfo.style.display = 'block'; // 잔여 PT 표시
		        // 일정 입력 폼은 초기엔 숨기고, 버튼 클릭 시 토글 처리
		        scheduleForm.style.display = userSelectionMode ? 'block' : 'none';
		        chatBox.style.display = 'none';
		        if (!userSelectionMode) {
		            ptButton.textContent = "일정 선택";
		        }
		        miniCalendar.updateSize();
		    }
		}
    });

    mainCalendar.render();
    miniCalendar.render();

    ptButton.addEventListener('click', function() {
        if (!userSelectionMode) {
            userSelectionMode = true;
            ptButton.textContent = "일정 선택 완료";
            mainCalendarEl.classList.add('selection-mode');
            scheduleForm.style.display = 'block';
        } else {
            var startStr = startDateInput.value;
            if (!startStr) {
                alert("시작 날짜를 선택해주세요.");
                return;
            }
            var startTime = new Date(startStr);
            var minutes = startTime.getMinutes();
            if (minutes !== 0 && minutes !== 30) {
                alert("분은 00분 또는 30분만 선택 가능합니다.");
                return;
            }
            var now = new Date();
            if (startTime < now) {
                alert("현재 시간 이후에만 스케줄을 생성할 수 있습니다.");
                return;
            }
            if (userCreatedEvents.length > 0) {
                userCreatedEvents[0].instance.remove();
                userCreatedEvents = [];
            }
            var endTime = new Date(startTime.getTime() + 60 * 60 * 1000);
            var newEventData = {
                 title: userName, // 'New Event' 대신 userName 사용
                start: startTime,
                end: endTime,
                allDay: false,
                color: 'yellow',
                isUserCreated: true
            };
            var addedEvent = mainCalendar.addEvent(newEventData);
            userCreatedEvents.push({ data: newEventData, instance: addedEvent });
            if (confirm("선택한 이벤트를 저장하시겠습니까?")) {
                userCreatedEvents.forEach(function(item) {
                    sendUserEventToServer(item.data, item.instance);
                });
          
            } else {
                userCreatedEvents.forEach(function(item) {
                    item.instance.remove();
                });
            }
            userCreatedEvents = [];
            userSelectionMode = false;
            ptButton.textContent = "일정 선택";
            mainCalendarEl.classList.remove('selection-mode');
            scheduleForm.style.display = 'none';
        }
    });

    function sendUserEventToServer(eventData, eventInstance) {
        $.ajax({
            url: '/calendar',
            type: 'GET',
            data: { 
                title: eventData.title,
                start: formatLocalDateTime(eventData.start),
                end: formatLocalDateTime(eventData.end),
                allDay: eventData.allDay,
				userId: userId ,
				trainerId:trainerId
            },
            dataType: 'text',
			success: function(response) {
			    if(response === 'success'){
			        alert('저장이 완료되었습니다.');
			    } else if(response === "noRange"){
			        alert('이용가능한 시간이 아닙니다.');
			        eventInstance.remove(); // 저장 실패 시 이벤트 삭제
			    } else if(response === "alreadySchedule"){
			        alert('이미 다른사람의 예약이 있습니다.');
			        eventInstance.remove(); // 저장 실패 시 이벤트 삭제
			    } else if(response === "alreadyHaveSchedule"){
					alert('이미 예약이 있습니다.');
					eventInstance.remove(); // 저장 실패 시 이벤트 삭제
				}
			},
            error: function() {
                eventInstance.remove();
            }
        });
    }
	
	
	// 삭제 버튼 이벤트 리스너 추가 (기존 deleteEventFromServer 관련 코드는 제거)
	var deleteButton = document.getElementById('delete-button');

	deleteButton.addEventListener('click', function() {
	    var now = new Date();
	    // fullCalendar에 등록된 모든 이벤트 중 조건에 맞는 이벤트 필터링
	    var eventsToDelete = mainCalendar.getEvents().filter(function(event) {
	        return event.extendedProps.userId &&
	               Number(event.extendedProps.userId) === Number(userId) &&
	               event.start >= now;
	    });

	    if (eventsToDelete.length === 0) {
	        alert("삭제 가능한 일정이 없습니다.");
	        return;
	    }

	    if (confirm("선택한 이벤트를 삭제하시겠습니까?")) {
	        eventsToDelete.forEach(function(event) {
				var eventData = {
				    scheduleId: event.extendedProps.scheduleId,
				    userId: event.extendedProps.userId,
				    title: event.title,
				    start: event.startStr,
				    end: event.endStr,
				    allDay: event.allDay
				};
	            // AJAX를 통해 삭제 요청 후,
	            ajaxDeleteEvent(eventData);
	            // 캘린더에서 해당 이벤트 제거
	            event.remove();
	        });
	    }
	});
	
	
	

	// 삭제 AJAX 함수 
	function ajaxDeleteEvent(eventData) {
	    $.ajax({
	        url: '/usercalendar/delete',
	        type: 'GET',
	        data: eventData,
	        dataType: 'json',
	        success: function(response) {
	            // 삭제가 성공하면 별도의 후처리 없이 콘솔에 메시지를 남기거나 추가 처리를 할 수 있습니다.
	            console.log("삭제 성공:", response);
	        },
	        error: function() {
	            console.error("삭제 요청 실패");
	        }
	    });
	}
});
