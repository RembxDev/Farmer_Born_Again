let stompClient = null;
let playerName = document.getElementById("playerName").textContent;
let gameId = document.getElementById("gameId").textContent;

function connectGame() {

    Object.keys(localStorage).forEach(key => {
        if (key.startsWith('morningLogs_') && !key.endsWith(gameId)) {
            localStorage.removeItem(key);
        }
    });

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
                    feedSpan.innerHTML = "🍗".repeat(event.feedLevel) + "▫️".repeat(5 - event.feedLevel);
                }

                if (event.extra && event.extra.siloKey && event.extra.siloValue !== undefined) {
                    const key = event.extra.siloKey;
                    const val = event.extra.siloValue;
                    const feedSiloSpan = document.getElementById("silo-" + key);
                    if (feedSiloSpan) {
                        feedSiloSpan.textContent = val;
                    }
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

        stompClient.subscribe('/topic/game/' + gameId + '/endTurn', function(message) {
            const status = JSON.parse(message.body);
            if (status.type === "NIGHT_TIME") {
                console.log("Zapada Noc!");
                window.location.href = "/farm/night";
            } else if (status.type === "NEW_MORNING") {
                console.log("Wstaje Słońce!");
                window.location.href = "/farm/morning";
            } else if (status.type === "DAY_TIME") {
                console.log("Można zacząć Dzień!");
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

function feedMultipleAnimals(ids) {
    ids.forEach(id => feedAnimal(id));
}

function toggleAnimalGroup(animalType) {
    const el = document.getElementById("group-" + animalType);
    if (el) el.style.display = el.style.display === "none" ? "block" : "none";
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
            alert("Błąd: nie udało się zakończyć tury");
        } else {
            console.log("Zakończono turę.");
        }
    });
}

// ALERTY
let alertQueue = [];
let isAlertShowing = false;

function showCustomAlert(message) {
    alertQueue.push(message);
    processAlertQueue();
}

function processAlertQueue() {
    if (isAlertShowing || alertQueue.length === 0) return;

    isAlertShowing = true;
    const message = alertQueue.shift();

    const alertBox = document.createElement('div');
    alertBox.classList.add('custom-alert');
    alertBox.innerHTML = `
        <span class="alert-message">${message}</span>
        <span class="alert-close">&times;</span>
    `;

    document.body.appendChild(alertBox);


    setTimeout(() => alertBox.classList.add('visible'), 10);


    alertBox.querySelector('.alert-close').addEventListener('click', () => {
        closeAlert(alertBox);
    });


    setTimeout(() => {
        if (document.body.contains(alertBox)) {
            closeAlert(alertBox);
        }
    }, 3500);
}

function closeAlert(alertBox) {
    alertBox.classList.remove('visible');
    setTimeout(() => {
        alertBox.remove();
        isAlertShowing = false;
        processAlertQueue();
    }, 400);
}

function toggleReadyButtonText(isFinished) {
    const btn = document.getElementById("readyButton");
    btn.textContent = isFinished
        ? "⬅️ Cofnij zakończenie tury"
        : "✅ Zakończ turę";
}

// DOM READY
document.addEventListener("DOMContentLoaded", () => {
    console.log("DOM załadowany");
    console.log("gameId:", gameId);

    const key = `morningLogs_${gameId}`;
    const morningLogs = localStorage.getItem(key);
    console.log("morningLogs content:", morningLogs);

    if (morningLogs) {
        try {
            const logs = JSON.parse(morningLogs);
            logs.forEach(showCustomAlert);
        } catch (err) {
            console.error("Błąd podczas parsowania morningLogs:", err);
        }
        localStorage.removeItem(key);
    }

    connectGame();
});
