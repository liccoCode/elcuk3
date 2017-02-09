/**
 * Created by licco on 2016/12/20.
 */
$(() => {
  $('#goToDeliverymentApply').click(function () {
    $('#deliverys_form').attr('method', 'post').attr('action', $(this).attr("url")).submit();
  });

  $("input[name='createInboundBtn']").click(function () {
    let ck = $("#dp_" + $(this).data("index") + " input[type='checkbox']:checked");
    let flag = true;
    if (ck.length > 0) {
      let firstProjectName = ck.eq(0).attr("project");
      ck.each(function () {
        if ($(this).attr("project") != firstProjectName) {
          noty({
            text: '项目名称必须一致！',
            type: 'error'
          });
          flag = false;
          return false;
        }
      });
      if (flag) {
        $('#deliverys_form').attr('method', 'post').attr('action', $(this).attr("url")).submit();
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

});
