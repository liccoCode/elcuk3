/**
 * Created by licco on 2016/11/30.
 */
$(() => {
  $("#confirmOutboundBtn").click(function(e) {
    e.stopPropagation();
    let num = $("input[name='ids']:checked").length;
    if (num == 0) {
      noty({
        text: '请选择需要出库的数据!',
        type: 'error'
      });
    } else if (confirm("确认出库 " + num + " 条出库单吗?")) {
        $("#submit_form").submit();
    }
  });

  $("select[name='outbound.type']").change(() => {
    showTypeSelect();
  });

  function showTypeSelect () {
    let type = $("select[name='outbound.type']").val();
    let $select = $("select[name='outbound.targetId']");
    switch(type) {
      case 'Normal' :
      case 'B2B' :
        $select.empty().append($("#shipperOptions").clone().html());
        break;
      case 'Refund' :
        $select.empty().append($("#supplierOptions").clone().html());
        break;
      case 'Process' :
        $select.empty().append($("#processOptions").clone().html());
        break;
      case 'Sample' :
        $select.empty().append($("#sampleOptions").clone().html());
        break;
      default:
        $select.empty();
    }
  }
  showTypeSelect();

  $("#printBtn").click(function(e) {
    e.stopPropagation();
    if ($("input[type='checkbox']:checked").length == 0) {
      noty({
        text: '请选择需要打印的出库单',
        type: 'error'
      });
      return;
    }
    let $form = $("#submit_form");
    window.open("/Outbounds/printOutboundForm?" + $form.serialize(), "_blank");
  });


});
