"use strict";

$(document).ready(() => {

    const urlToCreateElection = window.location.origin + '/create-election';

    const form = document.getElementById('login');
    const goal = document.getElementById('inputGoal');
    const voters = document.getElementById('inputVoters');
    const startDate = document.getElementById('inputStartDate');
    const endDate = document.getElementById('inputEndDate');
    const divChoices = document.getElementById('divChoices');
    const addButton = document.getElementById('addElection');
    const removeButton = document.getElementById('removeElection');

    function addChoice(num) {
        divChoices.innerHTML += `<label id="labelInputChoice${i}" for="${i}" class="sr-only">Choices</label>` +
            `<input type="text" id="${i}" class="form-control choice" placeholder="Choice" name="choice${i}" required>`;
    }

    let i = 0;
    for (; i < 2; i++) {
        addChoice(i);
    }

    addButton.addEventListener('click', function(event) {
        i += 1;
        addChoice(i);
    });

    removeButton.addEventListener('click', function(event) {
        if (i > 2) {
            divChoices.removeChild(document.getElementById(`labelInputChoice${i}`));
            divChoices.removeChild(document.getElementById(`${i}`));
            i -= 1;
        }
    });

    form.addEventListener('submit', async function(event) {
        event.preventDefault();

        const choices = []
        const inputContainers = document.querySelectorAll(".choice");
        inputContainers.forEach((container) => {
            choices.splice(Number(container.id), 0, container.value);
        });

        console.log()

        const data = {
            'goal': goal.value,
            'voters': voters.value,
            'startDate': startDate.value,
            'endDate': endDate.value,
            'choices': choices
        };

        $.ajax({
            type: "POST",
            url: urlToCreateElection,
            data: JSON.stringify(data),
            contentType: "application/json; charset=utf-8"
        }).done(function(response) {
            alert(response.message)
            if(response.success) {
                hide(document.querySelector("#div-error-sign-up"))
                window.location.href = response.url;
            } else {
                document.getElementById('error-create-election').innerHTML = response.message;
                show(document.querySelector("#div-error-create-election"));
            }
        }).fail(function(error) {
            alert(error.statusText)
            document.getElementById('error-create-election').innerHTML = 'Error ' + error.status + ': ' + error.statusText;
            show(document.querySelector("#div-error-create-election"));
        });
    })

    function hide(element) {
        element.classList.add('hidden');
    }
    function show(element) {
        element.classList.remove('hidden');
    }
});