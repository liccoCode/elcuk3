/**
 * Created by licco on 2016/12/20.
 */
$(() => {
  $('#goToDeliverymentApply').click(function() {
    $('#deliverys_form').attr('method', 'post').attr('action', $(this).attr("url")).submit();
  });

  $("input[name='createInboundBtn']").click(function() {
    if ($(this).data("ids") != "") {
      noty({
        text: '此出货单已经创建收货入库单【' + $(this).data("ids") + "】",
        type: 'error'
      });
    } else {
      $('#deliverys_form').attr('method', 'post').attr('action', $(this).attr("url")).submit();
    }

  });

});
