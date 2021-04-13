$(() => {
    var radios = $('.lti-student-quiz-questions-container [type=radio]');

    radios.each((index, radioBtn) => {
        radioBtn.addEventListener("click", function (ev) {
            var btn = $(ev.target);
            var inputs = btn.closest('.lti-student-quiz-question__answers').find('input');


            inputs.attr('disabled', true);

            $.ajax({
                type: "POST",
                url: `/api/v1/answerQuestion`,
                data: {
                    answerId: btn.attr('value')
                },
            })
            .done(function(resp) {
                switch (resp) {
                    case "false":
                        btn.prop("checked", false);
                        break;
                    case "end":
                        $('#finish-quiz-btn').click();
                        break;
                    case "true":

                        break;
                    default:
                        btn.prop("checked", false)
                        break;
                }
            })
            .always(function () {
                inputs.attr('disabled', false);
            });
        });
    });

    var $timeLimitLabel = $('#time-limit-label');
    var $timeLimitValue = $timeLimitLabel.next();

    var minuteString = $timeLimitLabel.attr('data-min-str');
    var startedDate = $timeLimitLabel.attr('data-started');
    var timeLimit = $timeLimitLabel.attr('data-time-limit');

    var finishDate = moment(startedDate).add(timeLimit, 'minutes');

    $timeLimitValue.text(moment(finishDate.diff(moment(), 'mm:ss')).format('mm:ss'));

    var countDown = setInterval(function () {
        $timeLimitValue.text(moment(finishDate.diff(moment(), 'mm:ss')).format('mm:ss'));
        if(moment().isAfter(finishDate)){
            $('#finish-quiz-btn').click();
            console.log('Finished quiz time');
            clearInterval(countDown);
        }
    },1000)

})
