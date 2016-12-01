/**
 * Created by licco on 2016/11/30.
 */

$(() => {

  $("select[name='outbound.type']").change(function(e) {
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

});
