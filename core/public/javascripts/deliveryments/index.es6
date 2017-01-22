/**
 * Created by licco on 2016/12/20.
 */
$(() => {
  $('#goToDeliverymentApply').click(function() {
    $('#deliverys_form').attr('method', 'post').attr('action', $(this).attr("url")).submit();
  });

  $("input[name='createInboundBtn']").click(function() {
    if ($("#dp_" + $(this).data("index") + " input[type='checkbox']:checked").length > 0) {
      $('#deliverys_form').attr('method', 'post').attr('action', $(this).attr("url")).submit();
    } else {
      noty({
        text: '请先选择需要收货入库的采购计划！',
        type: 'error'
      });
    }

  });

  $("input[name='checkAll']").change(function(e) {
    e.stopPropagation();
    let id = $(this).data("index");
    $("#" + id).find(':checkbox').prop('checked', $(this).prop('checked'));
  });

});
