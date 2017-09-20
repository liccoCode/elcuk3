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
    let firstCooperId = $("#payments_form input[name='pids']:checked").first().attr("cooperId");
    let i = 0;
    $("input[name='pids']:checked").each(function () {
      if ($(this).attr("cooperId") != firstCooperId) {
        noty({
          text: '请选择【供应商】一致的支付单！',
          type: 'error'
        });
        i++;
        return false;
      }
    });
    if (i == 0) {
      cbs.each(function () {
        ids.push($(this).val());
      });
      $("#payments_form").attr("action", $(this).data("url"));
      $("#payments_form").submit();
    }
  });

  $("#apply_form input[name='transferBtn']").click(function (e) {
    $("#status_input").val($(this).data("status"));
    $("#transfer_form").submit();
  });

  $("#endBtn").click(function (e) {
    $("#status_input").val('End');
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
      $("#div" + format_id).load($(this).data("url"), {id: id}, function () {});
    }
  });

  $("#printBtn").click(function (e) {
    let $form = $("#search_Form");
    window.open('/Excels/exportBatchReviewApply?' + $form.serialize(), "_blank")
  });

  function fidCallBack () {
    return {
      fid: $("input[name='applyId']").val(),
      p: 'BATCHAPPLY'
    }
  }
  let dropbox = $('#dropbox');
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1');
  window.dropUpload.iniDropbox(fidCallBack, dropbox);

});