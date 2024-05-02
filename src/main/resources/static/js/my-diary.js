document.addEventListener('DOMContentLoaded', function() {
    const dateButton = document.getElementById('date');
    const expendsList = document.getElementById('expends');

    function generateDateButtons() {
        const today = new Date();
        const day = today.getDate();
        const weeks = ['일', '월', '화', '수', '목', '금', '토'];

        for (let i = -3; i <= 3; i++) {
            const date = new Date(today);
            date.setDate(day + i);
            const week = date.getDay()
            const button = document.createElement('button');
            button.innerHTML = `${weeks[week]}<br>${date.getDate()}`;

            if (date.getDay() === 6) { //토요일
                button.style.color = 'blue';
            }
            else if (date.getDay() === 0) { //일요일
                button.style.color = 'red';
            }

            if (i===0){
                button.style.borderColor = '#1A4F32';
                button.style.borderWidth = '2px';
                button.style.borderStyle ='dotted';
            }

            button.onclick = function() {
                showExpend(date);
            };
            dateButton.appendChild(button);
        }
    }

    function showExpend(date) {
        const expendData = [
            {date: new Date(today.getFullYear(), today.getMonth(), today.getDate() - 1), time:'12:00', category: '식사', amount: '12000원', description: '점심 식사'},
            {date: new Date(today.getFullYear(), today.getMonth(), today.getDate()), time:'13:00', category: '교통', amount: '3500원', description: '지하철 카드 충전'},
            {date: new Date(today.getFullYear(), today.getMonth(), today.getDate()), time:'18:30', category: '쇼핑', amount: '59000원', description: '옷 구매'},
            {date: new Date(2024, 4, 4), time:'13:00', category: '교통', amount: '3500원', description: '점심먹기'},
            // monthidx는 0부터 시작해서 4.4해야 5월4일임
            {date: new Date(2024, 4, 4), time:'18:30', category: '쇼핑', amount: '59000원', description: '노래방가기'}
        ];

        const filterData = expendData.filter(item =>
            item.date.getFullYear() === date.getFullYear() &&
            item.date.getMonth() === date.getMonth() &&
            item.date.getDate() === date.getDate()
        );

        expendsList.innerHTML = '';

        const dateInfo = document.createElement('h4');
        dateInfo.textContent = `${date.getMonth() + 1}월 ${date.getDate()}일의 지출 내역`;
        expendsList.appendChild(dateInfo);


        if (filterData.length === 0) {
            expendsList.innerHTML += '해당 날짜에 대한 지출 내역이 없습니다.';
            return;
        }


        filterData.forEach(item => {
            const time = document.createElement('div');
            time.className = 'time';
            time.textContent = item.time;
            expendsList.appendChild(time);

            const expendBox = document.createElement('div');
            expendBox.className = 'expendBox';
            expendBox.innerHTML = `
            <p>${item.amount} ${item.description}</p>
            `;

            expendsList.appendChild(expendBox);

        });

        adjustHeight();
    }

    function adjustHeight() {
        const timelineBox = document.querySelector('.timelineBox');
        const lastExpendBox = document.querySelector('.expendBox:last-child');

        if (lastExpendBox) {
            const dividerHeight = lastExpendBox.offsetTop + lastExpendBox.offsetHeight - timelineBox.offsetTop;
            // 마지막 비용의 상단과 높이를 더해서 부모요소부터 박스까지의 상단 간격을 빼서 마지막 비용 항목의 아랫쪽 위치를 구할 수 있음.

            const divider = document.querySelector('.deco-flex .divider');
            divider.style.height = `${dividerHeight}px`;
        }
    }


    generateDateButtons();
    const today = new Date();
    showExpend(new Date(today.getFullYear(), today.getMonth(), today.getDate()));
});
