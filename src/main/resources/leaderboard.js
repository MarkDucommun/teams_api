var leaderboard = document.querySelector("#leaderboard");

var loader = document.querySelector(".loader");

var leaderboardRaw = null;

function getLeaderboard() {

    loader.classList.remove("hide");

    leaderboard.classList.add("hide");

    var url = "/leaderboard.json";

    var xhr = new XMLHttpRequest();

    xhr.open("GET", url, true);

    xhr.onreadystatechange = function () {

        if (xhr.readyState == 4 && xhr.status == 200) {

            leaderboardRaw = JSON.parse(xhr.response);

            leaderboardRaw.forEach(function(race) {
                console.log(race);

                var rank = document.createElement("div");
                rank.classList.add("Rtable-cell", "rank");

                var value = document.createElement("div");
                value.classList.add("Rtable-cell", "value");

                var players = document.createElement("div");
                players.classList.add("Rtable-cell", "players");

                rank.innerText = race.race.rank;
                value.innerText = race.race.score;
                players.innerText = race.race.players;

                leaderboard.appendChild(rank);
                leaderboard.appendChild(value);
                leaderboard.appendChild(players);
            });

            loader.classList.add("hide");

            leaderboard.classList.remove("hide");
        }
    };

    xhr.send();
}

getLeaderboard();