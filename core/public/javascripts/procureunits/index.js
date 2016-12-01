/**
 * Created by licco on 2016/11/14.
 */
$(() => {
  $("#createInboundBtn,#createOutboundBtn").click(function(e) {
    e.stopPropagation();
    if ($("input[name='pids']:checked").length == 0) {
      noty({
        text: '请选择需要下载的采购单元',
        type: 'error'
      });
      return false;
    } else {
      $.post('/Inbounds/createValidate', $("#create_deliveryment").serialize(), r => {

      });

      $("#create_deliveryment").attr("action", $(this).data("url")).submit();
    }
  });

});

