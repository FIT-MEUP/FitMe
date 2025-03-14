document.addEventListener('DOMContentLoaded', function () {
    var mainCalendarEl = document.getElementById('calendar');
    var miniCalendarEl = document.getElementById('mini-calendar');
    var miniCalendarWrapper = document.getElementById('mini-calendar-wrapper');
    var monthPlaceholder = document.getElementById('month-placeholder');
	var ptButton=document.getElementById('pt-button');
    var scheduleManagementButton = document.getElementById('schedule-management');
    var resetButton = document.getElementById('reset-button');
    var leftPlaceholder = document.getElementById('left-placeholder');

    var editingEnabled = false;
    var pendingCreations = [];
    var pendingDeletions = [];
	
	var memberManagementButton = document.getElementById('member-management');
	memberManagementButton.addEventListener('click', function () {
	    window.location.href = "trainer/memberManage";
		});
	scheduleManagementButton.addEventListener('click', function () {
	    editingEnabled = !editingEnabled;
	    if (editingEnabled) {
	        scheduleManagementButton.textContent = "가능 일정 완료";
	        scheduleManagementButton.classList.add('active');
	        resetButton.style.display = 'block';
	    } else {
	        scheduleManagementButton.textContent = "일정관리";
	        scheduleManagementButton.classList.remove('active');
	        resetButton.style.display = 'none';
	        
	        // 신규 생성된 이벤트들 처리
	        pendingCreations.forEach(function (item) {
	            sendEventToServer(item.data, item.instance);
	        });
	        pendingCreations = [];

	        // 삭제 대상 이벤트들 처리
	        if (pendingDeletions.length > 0) {
	            if (confirm("삭제할 이벤트 " + pendingDeletions.length + "개를 삭제하시겠습니까?")) {
	                pendingDeletions.forEach(function (item) {
	                    deleteEventFromServer(item.data);
	                });
	                pendingDeletions = [];
	            } else {
	                // 취소를 누른 경우, 삭제했던 이벤트들을 UI에 복원
	                pendingDeletions.forEach(function (item) {
	                    mainCalendar.addEvent(item.data);
	                });
	                pendingDeletions = [];
	            }
	        }
	    }
	});

    // 초기화 버튼 클릭 시, pending 변경사항 취소(원래 상태 복원)
    resetButton.addEventListener('click', function () {
        // 새로 생성한 이벤트는 UI에서 제거
        pendingCreations.forEach(function (item) {
            item.instance.remove();
        });
        // 삭제했던 이벤트는 UI에 다시 추가 (복원)
        pendingDeletions.forEach(function (item) {
            mainCalendar.addEvent(item.data);
        });
        pendingCreations = [];
        pendingDeletions = [];
    });

    var miniCalendar = new FullCalendar.Calendar(miniCalendarEl, {
        initialView: 'dayGridMonth',
        selectable: true,
        height: '300px',
        headerToolbar: false,
        dateClick: function (info) {
            mainCalendar.changeView('timeGridWeek', info.dateStr);
        }
    });

    var mainCalendar = new FullCalendar.Calendar(mainCalendarEl, {
		timeZone: 'UTC', // 여기에 타임존을 설정합니다.
		
        customButtons: {
            logout: {
                text: '로그아웃',
				click: function () {
								                window.location.href = '/user/logout?userId=' + trainerId;
								            }
            },
            personalInfo: {
                text: '개인정보',
				click: function () {
										window.location.href = '/trainer/' + realTrainerId;
								           }
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
        eventColor: '#3788d8',
        dateClick: function (info) {
            if (mainCalendar.view.type === 'dayGridMonth') {
                mainCalendar.changeView('timeGridWeek', info.dateStr);
            }
        },
        // 신규 이벤트 생성 (드래그)
        select: function (info) {
            if (mainCalendar.view.type === 'timeGridWeek' && editingEnabled) {
                var newEventData = {
                    title: "이용가능 시간",
                    start: info.startStr,
                    end: info.endStr,
                    allDay: info.allDay,
					trainerId:trainerId,
                    isNew: true,
                    editable: false
                };
                var addedEvent = mainCalendar.addEvent(newEventData);
                pendingCreations.push({ data: newEventData, instance: addedEvent });
            }
            mainCalendar.unselect();
        },
        // 이벤트 클릭 시 삭제 처리 (일정관리 모드에서)
        eventClick: function (info) {
            if (mainCalendar.view.type === 'timeGridWeek' && editingEnabled) {
                // pendingDeletions에 추가 후 UI에서 제거 (각 이벤트 당 confirm은 생략)
                var eventData = {
                    id: info.event.id,
                    title: info.event.title,
                    start: info.event.startStr,
                    end: info.event.endStr,
                    editable: info.event.extendedProps.editable
                };
                pendingDeletions.push({ data: eventData, instance: info.event });
                info.event.remove();
                // 만약 해당 이벤트가 pendingCreations에 있었다면 제거
                pendingCreations = pendingCreations.filter(function (item) {
                    return item.instance !== info.event;
                });
            }
        },
        eventDrop: function (info) {
            if (!editingEnabled) {
                alert('드래그 기능이 비활성화되어 있습니다.');
                info.revert();
                return;
            }
            if (!info.event.extendedProps.isNew) {
                alert('이미 생성된 일정은 드래그할 수 없습니다.');
                info.revert();
                return;
            }
        },
        eventResize: function (info) {
            if (!editingEnabled) {
                alert('리사이즈 기능이 비활성화되어 있습니다.');
                info.revert();
                return;
            }
            if (!info.event.extendedProps.isNew) {
                alert('이미 생성된 일정은 리사이즈할 수 없습니다.');
                info.revert();
                return;
            }
        },
        allDaySlot: false,
        views: {
            timeGridWeek: { allDaySlot: false },
            timeGridDay: { allDaySlot: false }
        },
        events: calendarEvents,
        datesSet: function (info) {
            var viewType = info.view.type;
            if (viewType === 'dayGridMonth') {
                miniCalendarWrapper.style.display = 'none';
                monthPlaceholder.style.display = 'block';
                scheduleManagementButton.style.display = 'none';
                leftPlaceholder.style.display = 'block';
                editingEnabled = false;
            } else if (viewType === 'timeGridWeek' || viewType === 'timeGridDay') {
                miniCalendarWrapper.style.display = 'block';
                monthPlaceholder.style.display = 'none';
                leftPlaceholder.style.display = 'none';
                if (viewType === 'timeGridWeek') {
                    scheduleManagementButton.style.display = 'block';
                    editingEnabled = false;
                    scheduleManagementButton.textContent = "일정관리";
                    scheduleManagementButton.classList.remove('active');
                    resetButton.style.display = 'none';
                } else {
                    scheduleManagementButton.style.display = 'none';
                    editingEnabled = false;
                }
                miniCalendar.updateSize();
            }
        }
    });

    mainCalendar.render();
    miniCalendar.render();

    function sendEventToServer(eventData, eventInstance) {
        $.ajax({
            url: '/trainercalendar',
            type: 'GET',
            data: eventData,
            dataType: 'json',
            success: function (response) {
                if (response === true) {
                    alert('입력에 성공하셨습니다');
                } else {
                    alert('입력 실패ㅠㅠ');
                }
            },
            error: function () {
                // 저장 오류 처리
            }
        });
    }

    function deleteEventFromServer(eventData) {
        $.ajax({
            url: '/trainercalendar/delete', // 삭제 전용 엔드포인트 (서버에 맞게 수정)
            type: 'GET', // DELETE 메서드 사용이 가능하면 'DELETE'로 변경 가능
            data: eventData,
            dataType: 'json',
            success: function (response) {
                if (response === true) {
                  
                } else {
                    alert('삭제 실패ㅠㅠ');
                }
            },
            error: function () {
                // 삭제 오류 처리
            }
        });
    }
	
	ptButton.addEventListener('click', function () {
		    $.ajax({
		       url: '/ptSessionHistoryChangeAmount',
		        type: 'GET',
		        data: { "trainerId": trainerId },
		        dataType: 'text',
		        success: function (response) {
					if(response="success"){
		            // 성공 후 처리 예시: 페이지를 새로고침하거나, 다른 페이지로 이동
		            alert('요청이 성공적으로 처리되었습니다.');
					}else{
						alert('지금은 PT 시작 10분전이 아닙니다.');
					}
		        },
		        error: function (xhr, status, error) {
		            console.error("요청 에러:", error);
		            alert("요청 처리 중 에러가 발생했습니다.");
		        }
		    });
		});
	
	
});
