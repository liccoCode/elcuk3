/**
 * Created by licco on 2016/12/12.
 */
$(() => {

  $("#printBtn").click(function(e) {
    e.stopPropagation();
    if ($("input[type='checkbox']:checked").length == 0) {
      noty({
        text: '请选择需要打印的入库单',
        type: 'error'
      });
      return;
    }
    let $form = $("#inboundForm");
    window.open("/Inbounds/printQuaternionForm?" + $form.serialize(), "_blank");
  });

});


