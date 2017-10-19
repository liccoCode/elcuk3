$(() => {



  //请款js处理
  $('#goToApply').click(function (e) {
    e.stopPropagation();
    let firstCooper = $("input[name='pids']:checked").first().attr("cooperName");
    if ($("input[name='pids']:checked").length == 0) {
      noty({
        text: '请选择需纳入请款的采购单(相同供应商)',
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
          text: '请选择[供应商]一致的出货单进行创建！',
          type: 'error'
        });
        return false;
      }

      if (i == 0 && j == 0) {
        $('#deliverys_form').attr('action', $(this).attr('url')).submit();
      }
    }
  });


  $("td[name='clickTd']").click(function () {
      let tr = $(this).parent("tr");
      let id = $(this).data("id");
      let format_id = id.replace(/\|/gi, '_');
      if ($("#div" + format_id).html() != undefined) {
        tr.next("tr").toggle();
      } else {
        let html = "<tr style='background-color:#F2F2F2'><td colspan='13'>";
        html += "<div id='div" + format_id + "'></div></td></tr>";
        tr.after(html);
        $("#div" + format_id).load($(this).data("url"), {id: id});
      }
    });

  

  //财务审核js处理
  $("#approveBatch").click(function (e) {
    let num = $("input[name='pids']:checked").length;
    if (num == 0) {
      noty({
        text: '请选择需要审核的数据!',
        type: 'error'
      });
    } else {
      let i = 0;
      let ids = [];
      $("input[name='pids']:checked").each(function () {
        console.log($(this).attr("financeState"));
        if ($(this).attr("financeState") != "PENDING_REVIEW") {
          i++;
          noty({
            text: $(this).val() + '已经审核，请选择【待审核】的出货单',
            type: 'error'
          });
          return false;
        }
        ids.push($(this).val());
      });
      if (i == 0) {
        if (confirm("点击审核后，即表示出货单据已审核通过！")) {
          return $('#deliverys_form').attr('action', $(this).data('url')).submit();
        }
      }
    }
  });

  //单个数据财务审核js处理
  $("#data-table a[name='approveBtn']").click(function (e) {
    e.preventDefault();
    $("#planId").val($(this).attr('uid'));
    if (confirm("点击审核后，即表示出货单据已审核通过！")) {
      return $('#deliverys_form').attr('action', $(this).data('url')).submit();
    }
  });

});