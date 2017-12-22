var image = document.querySelector("#image");

var loader = document.querySelector("#loader");

var score = document.querySelector("#score");

var submit = document.querySelector("#submit");

var lastRace = null;

function getLastRace() {

    var url = "/races/last";

    var xhr = new XMLHttpRequest();

    xhr.open("GET", url, true);

    xhr.onreadystatechange = function () {

        if (xhr.readyState == 4 && xhr.status == 200) {

            var lastRace = JSON.parse(xhr.response);

            image.src = lastRace.race.image.path;
        }
    };

    xhr.send();
}

getLastRace();