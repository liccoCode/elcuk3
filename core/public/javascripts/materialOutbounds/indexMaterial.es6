/**
 * Created by licco on 2016/11/14.
 */
$(() => {
  $('#createOutboundBtn').click(function (e) {
    e.stopPropagation();
    let $btn = $(this);
    if ($("input[name='pids']:checked").length == 0) {
      noty({
        text: '请选择需要操作的物料',
        type: 'error'
      });
      return false;
    } else  {
      $("#outboundForm").attr("action", $btn.data("url")).submit();
    }
  });

  
});

