/**
 * Created by licco on 2016/11/14.
 */
$(() => {
  $('#createPlanBtn').click(function (e) {
    e.stopPropagation();
    let firstCooper = $("input[name='pids']:checked").first().attr("cooperName");
    if ($("input[name='pids']:checked").length == 0) {
      noty({
        text: '请选择需要操作的物料',
        type: 'error'
      });
      return false;
    } else {

      let i = 0;
      let j = 0;
      let ids = [];
      $("input[name='pids']:checked").each(function () {
        if ($(this).attr("cooperName") != firstCooper) {
          j++;
        }
        ids.push($(this).val());
      });
      if (j > 0) {
        noty({
          text: '请选择[供应商]一致的物料编码进行创建！',
          type: 'error'
        });
        return false;
      }

      if (i == 0 && j == 0) {
        $("#name_form").val(name);
        $("#planForm").submit();
      }
    }
  });

});

