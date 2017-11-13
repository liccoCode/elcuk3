$(() => {

  $("a[name='unitUpdateBtn']").click(function () {
    let job = $(this).data('job');
    $("#jobName").val(job);
    $('#job_form').submit();

  });
  
});