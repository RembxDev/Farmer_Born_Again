
let stompClient = null;
let playerName = document.getElementById("playerName").textContent;
let gameId = document.getElementById("gameId").textContent;

function connectGame() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Połączono z sesją gry, frame: ' + frame);

        stompClient.subscribe('/topic/game/' + gameId, function(message) {
            const event = JSON.parse(message.body);
            console.log("Odebrano komunikat z gry:", event);
            addGameLog(event.description);
        });

        stompClient.subscribe('/topic/game/'+ gameId + '/endTurn', function(message) {
            const status = JSON.parse(message.body);
            if (status.type === "NIGHT_TIME") {
                console.log("Zapada Noc!...");
                window.location.href = "/farm/night";
            } else if (status.type === "NEW_MORNING") {
                console.log("Wstaje Słońce!...");
                window.location.href = "/farm/morning";
            } else if (status.type === "DAY_TIME") {
                console.log("Można zacząć Dzień!...");
                window.location.href = "/farm/";
            }
        });
    });
}

function sendFeedAction() {
    if (stompClient) {
        const actionEvent = {
            action: "FEED_ANIMAL",
            player: playerName,
            targetId: 42,
            description: playerName + " nakarmił zwierzę o ID 42"
        };
        console.log("Wysyłanie akcji:", actionEvent);
        stompClient.send("/app/game/" + gameId + "/action", {}, JSON.stringify(actionEvent));
    } else {
        console.warn("Brak połączenia WebSocket. Akcja nie została wysłana.");
    }
}

function addGameLog(message) {
    const gameLog = document.getElementById('gameLog');
    const logEntry = document.createElement('div');
    logEntry.textContent = message;
    gameLog.appendChild(logEntry);
    gameLog.scrollTop = gameLog.scrollHeight;
}

function markReady() {
    fetch("/farm/ready", {
        method: "POST"
    }).then(res => {
        if (!res.ok) {
            alert("Błąd: nie udało się oznaczyć zakonczyć tury");
        } else {
            console.log("Zakończono Ture.");
        }
    });
}

document.addEventListener('DOMContentLoaded', function() {
    console.log("Ładowanie strony, nawiązywanie połączenia z sesją gry...");
    connectGame();
});
