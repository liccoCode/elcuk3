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

  $("#categories").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '品线',
    maxHeight: 200,
    includeSelectAllOption: true
  });

});