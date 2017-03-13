/**
 * Created by licco on 2017/3/13.
 */


$(() => {

  $("#exportBtn").click(function (e) {
    e.preventDefault();
    let $btn = $(this);
    let form = $('<form method="post" action=""></form>');
    form.attr('action', $btn.data('url')).attr('target', $btn.data('target'));
    form.hide().append($btn.parents('form').find(":input").clone()).appendTo('body');
    form.submit().remove();
  });

  $("#whouse").multiselect({
    buttonWidth: '150px',
    nonSelectedText: '仓库',
    maxHeight: 200,
    includeSelectAllOption: true
  });

  $(document).ready(function () {
    $("#unit_table").dataTable({
      "sDom": "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>",
      "sPaginationType": "full_numbers",
      "iDisplayLength": 50,
      "aaSorting" :[[0,'desc']]
    });
  });

});