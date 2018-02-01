$(() => {


    $("#syncOrderr").click(function (e) {
        $("#syncOrderr_modal").modal('show')
    });

    $("a[name='unitUpdateBtn']").click(function () {
        let job = $(this).data('job');
        let estype = $(this).data('estype');
        $("#jobName").val(job);
        $("#esType").val(estype);
        $('#job_form').submit();
    });


    $("#submitSyncOrderBtn").click(function (e) {
        $.post('/jrockends/syncOrderr', {
            reportId: $("#reportId").val(),
            market: $("#market").val()
        }, function (r) {
            if (r.flag) {
                noty({
                    text: '同步成功!',
                    type: 'success'
                });

                $("#syncOrderr_modal").modal('hide')
            } else {
                noty({
                    text: r.message,
                    type: 'error'
                });
            }
        });
    });
    



});