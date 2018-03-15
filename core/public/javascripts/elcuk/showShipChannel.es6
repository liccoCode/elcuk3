$(() => {

  $("#addChannelBtn").click(function () {
    $("#channelId").val("");
    $("input[name='detail.channel']").val("");
    $("select[name='detail.internationExpress']").val("");
    $("select[name='detail.type']").val("");
    $("#channel_modal").modal("show");
  });

  $("button[name='updateChannelBtn']").click(function () {
    $("#channelId").val($(this).data("id"));
    $("input[name='detail.channel']").val($(this).data("channel"));
    $("select[name='detail.internationExpress']").val($(this).data("internation"));
    $("select[name='detail.type']").val($(this).data("type"));
    $("#channel_modal").modal("show");
  });

});