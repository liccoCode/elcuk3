$(() => {

  $("#b2b_select").change(function () {
    let id = $(this).val();
    $.get($(this).data("url"), {id: id}, function (r) {
      $("input[name='ship.receiverPhone']").val(r['receiverPhone']);
      $("input[name='ship.receiver']").val(r['receiver']);
      $("input[name='ship.countryCode']").val(r['countryCode']);
      $("input[name='ship.postalCode']").val(r['postalCode']);
      $("input[name='ship.city']").val(r['city']);
      $("input[name='ship.address']").val(r['address']);
    });
  });

  $("#improtPayment").click(function (e) {
    e.preventDefault();
    $("#payment_modal").modal('show');
    e.preventDefault();
  });

  $("#submitUpdateBtn").click(function () {
    $("#payment_form").submit();
  });

  $("#showTrackNo").click(function () {
    $("#show_trackNo_modal").modal("show");
  });

  //trace_no新增一行
  $("#more_trackno_btn").click(function () {
    let $btn = $(this);
    let $table = $("#trackno_table");
    let trs = $table.find("tr");
    $(trs[trs.size() - 1]).before(
    "<tr><td><input type='text' style='width:200px' class='form-control' name='ship.tracknolist[" + (trs.size() - 1) + "]'> " +
    "<a class='btn btn-danger' name='delete_trackno_row'><i class='icon-remove'></a>" +
    "</td></tr>");
  });

  $("#trackno_table a[name='delete_trackno_row']").click(function () {
    let $btn = $(this);
    $btn.parent("td").parent().remove();
    let trs = $("table[id=trackno_table]").find("tr");
    $.each(trs, function (index, tr) {
      let $tr = $(tr);
      $tr.find("input").attr("name", "ship.tracknolist[" + index + "]")
    });
  });

  $("#internationExpressSelect").change(function () {
    let internationExpress = $(this).val();
    let type = $("#shipTypeInput").val();
    $.post($(this).data("url"), {
      internationExpress: internationExpress,
      type: type
    }, function (r) {
      $("#channelSelect").empty();
      $("#channelSelect").append("<option value=''>请选择</option>");
      $.each(r, function (index, data) {
        $("#channelSelect").append("<option value='" + data + "'>" + data + "</option>");
      });
    });
  });

  $("a[name='fbabg']").click(function () {
    let $btn = $(this);
    $btn.parent("td").attr("bgcolor", "#FBE1B6");
    let url = $btn.attr("href");
    window.open(url, "_blank");
  });

  let tackNo = $("input[name='ship.tracknolist[0]']").val();
  let fc = $("#fc_input").val();

  YQV5.trackSingle({
    //必须，指定承载内容的容器ID。
    YQ_ContainerId: "YQContainer",
    //可选，指定查询结果高度，最大为800px，默认为560px。
    YQ_Height: 560,
    //可选，指定运输商，默认为自动识别。
    YQ_Fc: fc,
    //可选，指定UI语言，默认根据浏览器自动识别。
    YQ_Lang: "zh-cn",
    //必须，指定要查询的单号。
    YQ_Num: tackNo
  });
});