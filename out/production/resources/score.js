var image = document.querySelector("#image");

var loader = document.querySelector(".loader");

var score = document.querySelector("#score");

var submit = document.querySelector("#submit");

var lastRace = null;

function getLastRace() {

    loader.classList.remove("hide");

    image.classList.add("hide");

    score.classList.add("hide");

    submit.classList.add("hide");

    var url = "/races/last-unscored";

    var xhr = new XMLHttpRequest();

    xhr.open("GET", url, true);

    xhr.onreadystatechange = function () {

        if (xhr.readyState == 4 && xhr.status == 200) {

            if (xhr.response == "no more unscored races") {

                window.location = "/upload"

            } else {
                lastRace = JSON.parse(xhr.response);

                image.src = lastRace.race.image.path;

                loader.classList.add("hide");

                image.classList.remove("hide");

                score.classList.remove("hide");

                submit.classList.remove("hide");
            }
        }
    };

    xhr.send();
}

function submitScore() {

    loader.classList.remove("hide");

    image.classList.add("hide");

    score.classList.add("hide");

    submit.classList.add("hide");

    var url = "/scores?raceId=" + lastRace.race.id + "&score=" + score.selectedOptions[0].value;

    var xhr = new XMLHttpRequest();

    xhr.open("POST", url, true);

    xhr.onreadystatechange = function () {

        if (xhr.readyState == 4 && xhr.status == 200) {

            getLastRace();
        }
    };

    xhr.send();
}

function populateSelect() {

    for (var i = 244; i >= 81; i--) {
        var option = document.createElement("option");
        option.value = i.toString();
        option.innerText = i.toString();
        if (i == 200) option.selected = true;
        score.appendChild(option)
    }
}

submit.addEventListener("click", function() {
   submitScore();
});

getLastRace();
populateSelect();