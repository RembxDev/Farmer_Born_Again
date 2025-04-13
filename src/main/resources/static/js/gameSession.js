
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


            if (event.action === "FEED_ANIMAL" && event.feedLevel !== undefined && event.targetId !== undefined) {
                const feedSpan = document.getElementById("feedLevel-" + event.targetId);
                if (feedSpan) {
                    feedSpan.textContent = event.feedLevel;
                }
            }

            if (event.action === "TURN_PROGRESS") {
                const turnStatus = document.getElementById("turnStatus");
                if (turnStatus) {
                    turnStatus.textContent = event.description;
                }


                if (event.player === playerName) {
                    const btn = document.getElementById("readyButton");
                    const currentlyFinished = btn.textContent.includes("Cofnij");
                    toggleReadyButtonText(!currentlyFinished);
                }
            }
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

function feedAnimal(animalId) {
    if (!stompClient || !stompClient.connected) {
        console.warn("Brak połączenia WebSocket.");
        return;
    }

    const actionEvent = {
        action: "FEED_ANIMAL",
        player: playerName,
        targetId: animalId,
        description: playerName + " nakarmił zwierzę o ID " + animalId
    };

    console.log("🔼 Wysyłam akcję karmienia:", actionEvent);
    stompClient.send("/app/game/" + gameId + "/action", {}, JSON.stringify(actionEvent));
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

function toggleReadyButtonText(isFinished) {
    const btn = document.getElementById("readyButton");
    if (isFinished) {
        btn.textContent = "⬅️ Cofnij zakończenie tury";
    } else {
        btn.textContent = "✅ Zakończ turę";
    }
}


