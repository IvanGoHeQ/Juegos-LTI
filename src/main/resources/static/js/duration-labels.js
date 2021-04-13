$(() => {
    var labelsWithDuration = $('[data-seconds-taken]');
    labelsWithDuration.each((idx, el) => {
        var $el = $(el);
        var secondsValue = parseInt($el.attr('data-seconds-taken'));
        if(secondsValue === -1 || isNaN(secondsValue)){
            $el.text(
                '--:--' + ' ' + $el.attr('data-min-str')
            );
        }else{
            $el.text(
                moment.utc(
                    moment.duration(secondsValue,'seconds')
                    .as('milliseconds')
                )
                .format('mm:ss') + ' ' + 
                $el.attr('data-min-str')
            );
        }
    })
})
