# 🐑 Super Farmer: Born Again

**Super Farmer: Born Again** to gra ekonomiczno-strategiczna online dla 2–8 graczy, w której każdy z uczestników zarządza własną farmą, podejmuje decyzje, hoduje zwierzęta, handluje i walczy o tytuł Najlepszego Farmer(a)!

---

## 🎮 Zasady Gry

### 👥 Liczba Graczy
2–8 graczy

---

## 🏡 Farma Gracza

Każdy gracz zarządza własną farmą zawierającą:
- 🏠 **Dom Farmer(a)** – miejsce kończenia akcji w turze
- 🌾 **Silos** – pokazuje zapasy paszy
- 🐇 **Zagroda** – zawiera zwierzęta
- 📦 **Magazyn** – pozostałe zasoby i produkty
- 🛣️ **Droga na Rynek** – miejsce handlu i zakupów

---

## 🧾 Zasoby Początkowe
- 2 🐇 Króliki
- 5 paszy niskiej jakości

---

## 🐾 Zwierzęta i Produkcja

| Nazwa     | Pozyskanie               | Produkcja | Potrzeby              | Sprzedaż                          |
|-----------|--------------------------|-----------|------------------------|-----------------------------------|
| Królik    | Na start / Rozmnożenie   | Brak      | Brak                   | 1 Niska pasza                     |
| Kura      | Wymiana / Rozmnożenie    | Jaja      | Niska pasza            | 4 Niskie pasze                    |
| Owca      | Wymiana / Rozmnożenie    | Wełna     | Średnia pasza          | 8 Średniej paszy                  |
| Krowa     | Wymiana / Rozmnażanie    | Mleko     | Wysoka pasza           | 25 Wysokiej paszy                 |
| Koń       | Wymiana / Rozmnażanie    | Wyścigi   | Wysoka pasza + zagroda | 40 Wysokiej paszy                 |
| Świnia    | Zakup / Rozmnażanie      | Tuczenie | Średnia/Wysoka pasza   | 10 lub 20 w zależności od tuczu   |
| Pies (duży) | Zakup                   | Brak      | Brak                   | Brak                              |
| Pies (mały) | Zakup                   | Brak      | Brak                   | Brak                              |

---

## 🔁 Mechanika Wymiany

- 5 🐇 Królików = 1 🐔 Kura  
- 4 🐔 Kury = 1 🐑 Owca  
- 4 🐑 Owce = 1 🐄 Krowa  
- 3 🐄 Krowy = 1 🐎 Koń  

---

## ⏳ Przebieg Tury

Każda tura składa się z 3 faz:
1. **🌅 Nowy Ranek** – rzut kośćmi (rozmnażanie / atak lisa)
2. **🌞 Dzień Farmer(a)** – gracz podejmuje akcje: handel, karmienie, zakup, rozmnażanie
3. **🌙 Noc** – losowy event wpływający na rozgrywkę

---

## 🎲 Mechanika Rozmnażania

**Rzut kośćmi** (3 razy) – jeśli wypadnie zwierzę, sprawdzane są warunki:
- co najmniej 2 sztuki tego zwierzęcia
- nie są chore
- mają najedzenie min. 4/5
- jest miejsce w zagrodzie

| Zwierzę  | Szansa rozmnożenia |
|----------|---------------------|
| Królik   | 80%                |
| Kura     | 60%                |
| Owca     | 50%                |
| Krowa    | 40%                |
| Koń      | 10%                |
| Świnia   | 40%                |

Eventy wpływające na rozmnażanie:
- 🌤️ *Miła pogoda* – +10% szans
- 🌧️ *Zła pogoda* – -10% szans

---

## 📦 Sklepy

### 🏪 Sklep 1: Handel
- Sprzedaż zwierząt i produktów
- Ceny zależne od eventów i wcześniejszego handlu

Początkowe ceny:
- 🥚 Jajka – 2 Niskie pasze  
- 🧶 Wełna – 4 Średnie pasze  
- 🥛 Mleko – 6 Wysokie pasze  

### 🌽 Sklep 2: Pasza
- Zakup paszy różnych jakości

Ceny początkowe:
- Średnia – 2 Niskie pasze  
- Wysoka – 2 Średnie pasze

### 🐖 Sklep 3: Zwierzęta
- Zakup świń i psów

Ceny:
- Świnia – 15 Średniej paszy  
- Pies (mały) – 15 Wysokiej paszy  
- Pies (duży) – 30 Wysokiej paszy  

### 🛠️ Sklep 4: Rozbudowa
- Rozszerzenie farmy

| Ulepszenie         | Koszt                        |
|--------------------|------------------------------|
| Zagroda            | 5 Wełna + 3 Mleko            |
| Silos              | 10 Jajka + 3 Wełna           |
| Zagroda dla koni   | 10 Mleko + 5 Wełna           |

---

## 🎯 Cel Gry

Wygrać prestiżowy konkurs **„Ja i moja Farma”**, który odbywa się po określonym czasie gry. Zwycięża gracz z najlepiej rozwiniętą farmą wg ustalonych kryteriów punktowych.

---

## 💻 Technologie
- Java 17, Spring Boot
- WebSocket (STOMP)
- Thymeleaf
- HTML/CSS/JS (Vanilla)

---
