/**
 * Created by licco on 2016/12/12.
 */
$(() => {

  $("#printBtn").click(function (e) {
    e.stopPropagation();
    if ($("#inboundForm input[type='checkbox']:checked").length == 0) {
      noty({
        text: '请选择需要打印的入库单',
        type: 'error'
      });
      return;
    }
    let $form = $("#inboundForm");
    window.open("/Inbounds/printQuaternionForm?" + $form.serialize(), "_blank");
  });

  $("td[name='clickTd']").click(function () {
    let tr = $(this).parent("tr");
    let id = $(this).data("id");
    let format_id = id.replace(/\|/gi, '_');
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='11'><hr>";
      html += "<div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#div" + format_id).load("/Inbounds/showProcureUnitList", {id: id}, function () {
        $("input[name='editBoxInfo']").click(function (e) {
          e.stopPropagation();
          let id = $(this).data("id");
          refreshDiv(id);
        });
      });
    }
  });

  function refreshDiv (ids) {
    $("#refresh_div").load("/Inbounds/refreshFbaCartonContentsByIds", {ids: ids}, function () {
      $("#fba_carton_contents_modal").modal('show');
      $.getScript('/public/javascripts/inbounds/boxInfo.js');
    });
  }

  $("#submitBoxInfoBtn").click(function (e) {
    e.stopPropagation();
    let action = $(this).data('action');
    let form = $("<form method='post' action='#{action}'></form>")
    form = form.append($("#box_info_table").clone())
    $.post('/Inbounds/updateBoxInfo', form.serialize(), function (re) {
      if (re) {
        noty({
          text: '更新包装信息成功!',
          type: 'success'
        });
        window.location.reload();
      } else {
        noty({
          text: r.message,
          type: 'error'
        });
      }
    });
  });

  $("#exportBtn").click(function (e) {
    e.preventDefault();
    let $form = $("#search_Form");
    window.open('/Excels/exportInboundUnitReport?' + $form.serialize(), "_blank")
  });

  $("#categories").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '品线',
    maxHeight: 200,
    includeSelectAllOption: true
  });
  
});


