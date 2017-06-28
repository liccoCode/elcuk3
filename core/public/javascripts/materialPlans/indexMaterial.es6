/**
 * Created by licco on 2016/11/14.
 */
$(() => {
  $('#createPlanBtn').click(function (e) {
    e.stopPropagation();
    let name = $("#name_input").val();
    if ($("input[name='pids']:checked").length == 0) {
      noty({
        text: '请选择需要操作的物料',
        type: 'error'
      });
      return false;
    } else if (name == null || name =='') {
      noty({
        text: '请输入物料出货单的别名',
        type: 'error'
      });
      return false;
    } else {
      $("#name_form").val(name);
      $("#planForm").submit();
    }
  });

});

