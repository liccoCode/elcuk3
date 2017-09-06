/**
 * Created by licco on 2016/12/20.
 */
$(() => {
  $('#goToDeliverymentApply').click(function () {
    $('#data_table').attr('method', 'post').attr('action', $(this).attr("url")).submit();
  });

  $("input[name='createInboundBtn']").click(function () {
    let ck = $("#dp_" + $(this).data("index") + " input[type='checkbox']:checked");
    if (ck.length > 0) {
      let form = $('<form method="post" action=""></form>');
      form.attr('action', $(this).attr("url"));
      form.hide().append($("#dp_" + $(this).data("index")).find(":input").clone()).appendTo('body');
      form.submit();
    } else {
      noty({
        text: '请先选择需要收货入库的采购计划！',
        type: 'error'
      });
    }
  });

  $("#batchReceiveBtn").click(function (e) {
    e.preventDefault();
    let ck = $("#data_table input[name='pids']:checked");
    let flag = true;
    if (ck.length > 0) {
      let firstCooper = ck.eq(0).attr("cooper");
      ck.each(function () {
        if ($(this).attr("cooper") != firstCooper) {
          noty({
            text: '供应商必须一致！',
            type: 'error'
          });
          flag = false;
          return false;
        }
      });
      if (flag) {
        $('#deliverys_form').attr('method', 'post').attr('action', $(this).data("url")).submit();
      }
    } else {
      noty({
        text: '请先选择需要收货入库的采购计划！',
        type: 'error'
      });
    }
  });

  $("input[name='checkAll']").change(function (e) {
    e.stopPropagation();
    let id = $(this).data("index");
    $("#" + id).find(':checkbox').prop('checked', $(this).prop('checked'));
  });

  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id.replace(/\|/gi, '_');
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='13'>";
      html += "<div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#div" + format_id).load($(this).data("url"), {id: id});
    }
  });

});
