$(() => {

  $("#exportBtn").click(function (e) {
    e.preventDefault();
    let $btn = $(this);
    let form = $('<form method="post" action=""></form>');
    form.attr('action', $btn.data('url')).attr('target', $btn.data('target'));
    form.hide().append($btn.parents('form').find(":input").clone()).appendTo('body');
    form.submit().remove();
  });

  $("#batchApplyBtn").click(function (e) {
    let ids = [];
    let cbs = $("#payments_form input[name='pids']:checked");
    if (cbs.length == 0) {
      noty({
        text: '请选择需要核单的请款单',
        type: 'error'
      });
      return;
    }
    cbs.each(function () {
      ids.push($(this).val());
    });
    $("#payments_form").attr("action", $(this).data("url"));
    $("#payments_form").submit();
  });

  $("#apply_form input[name='transferBtn']").click(function (e) {
    $("#status_input").val($(this).data("status"));
    $("#transfer_form").submit();
  });

  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id.replace(/\|/gi, '_');
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='11'>";
      html += "<div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#div" + format_id).load($(this).data("url"), {id: id}, function () {
        $("input[name='editBoxInfo']").click(function (e) {
          e.stopPropagation();
          let id = $(this).data("id");
          refreshDiv(id);
        });
      });
    }
  });

});