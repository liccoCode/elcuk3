/**
 * Created by licco on 2016/12/20.
 */
$(() => {
  $('#goToDeliverymentApply').click(function() {
    $('#deliverys_form').attr('method', 'post').attr('action', $(this).attr("url")).submit();
  });

  $("input[name='createInboundBtn']").click(function() {
    $('#deliverys_form').attr('method', 'post').attr('action', $(this).attr("url")).submit();
  });

});
