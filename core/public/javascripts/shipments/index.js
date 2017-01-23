/**
 * Created by licco on 2016/12/6.
 */
$(() => {
  $('#createApplyBtn').click(function() {
    /*过滤掉apply为空的数据*/
    let $ck = $("#shipmentTable [type='checkbox']:checked");
    let size = $ck.length;
    let i = 0;
    $ck.each(() => {
      if ($(this).attr("apply")) {
        $(this).prop("checked", false);
        i++;
      }
      if (i == size && size != 0) {
        noty({
          text: "您选择的运输单全部都已经创建过请款单了，请重新选择！",
          type: 'warning'
        });
      } else {
        $('#search_form').attr('action', $(this).data('url')).submit();
      }
    });
  });

  $("#download_excel").click(function(e) {
    e.preventDefault();
    let $form = $("#search_form");
    let express_size = 0;
    let other_size = 0;
    $("#shipmentTable input[name='shipmentId']:checked").each(() => {
      if ($(this).attr("way") == 'EXPRESS') {
        express_size++
      } else {
        other_size++
      }
    });

    if (other_size > 0 && express_size > 0) {
      noty({
        text: "快递不可与海空运运输同时导出，请重新选择！",
        type: 'warning'
      });
      return;
    }
    let form = $("#shipmentTable input[name='shipmentId']:checked");
    window.open('/Excels/shipmentDetails?' + $form.serialize() + "&" + form.serialize(), "_blank");
  });

  $("#outboundBtn").click(function(e) {
    e.stopPropagation();
    let $btn = $(this);
    let checkboxs = $btn.parents('form').find("input:checkbox[name='shipmentId']:checked");
    if (checkboxs.length == 0) {
      noty({
        text: "请选择运输单！",
        type: 'warning'
      });
      return;
    } else {
      let flag = 0;
      let shipmentIds = [];
      checkboxs.each(function() {
        let ck = $(this);
        if (ck.attr("coop") == "") {
          noty({
            text: "运输单【" + ck.val() + "】未填运输商！",
            type: 'warning'
          });
          flag++;
          return false;
        }
        if (ck.attr("outId") != "") {
          noty({
            text: "运输单【" + ck.val() + "】已经创建出库单【" + ck.attr("outId") + "】！",
            type: 'warning'
          });
          flag++;
          return false;
        }
        shipmentIds.push(ck.val())
      });
      if (valid() && flag == 0) {
        $.get('/shipments/validCreateOutbound', {shipmentIds: shipmentIds}, function(r) {
          if (r.flag) {
            let $form = $('<form method="post" action=""></form>');
            $form.attr('action', $btn.data('url')).attr('target', $btn.data('target'));
            $form.hide().append(checkboxs.clone()).appendTo('body');
            $form.submit().remove();
          } else {
            if (confirm(r.message)) {
              let $form = $('<form method="post" action=""></form>');
              $form.attr('action', $btn.data('url')).attr('target', $btn.data('target'));
              $form.hide().append(checkboxs.clone()).appendTo('body');
              $form.submit().remove();
            }
          }
        });
      }
    }
  });

  function valid () {
    if ($("input[name='shipmentId']:checked").length > 1) {
      let firstProjectName = $("input[name='shipmentId']:checked").first().attr("project");
      let firstCountry = $("input[name='shipmentId']:checked").first().attr("country");
      let firstShipType = $("input[name='shipmentId']:checked").first().attr("way");
      let firstCompany = $("input[name='shipmentId']:checked").first().attr("company");
      let i = 0;
      $("input[name='shipmentId']:checked").each(function() {
        if ($(this).attr("project") != firstProjectName || $(this).attr("company") != firstCompany
        || $(this).attr("country") != firstCountry || $(this).attr("way") != firstShipType) {
          i++;
        }
      });
      if (i > 0) {
        noty({
          text: '请选择相同【项目名称】【运输方式】【货代公司】【去往仓库】的采购单元',
          type: 'error'
        });
        return false;
      }
    }
    return true;
  }

  $(':checkbox[class=checkbox_all]').change(function(e) {
    $ck = $(this);
    $ck.parents('table').find(':checkbox').not(':first').prop("checked", $ck.prop('checked'));
  });

  $("td[name='clickTd']").click(function() {
    let tr = $(this).parent("tr");
    let shipment_id = $(this).attr("shipment_id");
    let format_id = shipment_id.replace(/\|/gi, '_');
    let memo = $(this).attr("memo");
    if ($("#div" + format_id).html() != undefined) {
      tr.next("tr").toggle();
    } else {
      let html = "<tr style='background-color:#F2F2F2'><td colspan='14'><div><h4 class='text-info'>Comment</h4>" + memo + "</div><hr>";
      html += "<div id='div" + format_id + "'></div></td></tr>";
      tr.after(html);
      $("#div" + format_id).load("/Shipments/showProcureUnitList", {id: shipment_id});
    }
  });

  $("#shipmentTable").on("click", "input[name='editBoxInfo']", function(e) {
    e.stopPropagation();
    $("#fba_carton_contents_modal").modal('show');
    let id = $(this).data("id");
    $("#refresh_div").load("/ProcureUnits/refreshFbaCartonContentsByIds", {id: id});
  });

  $("#states").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '状态',
    maxHeight: 200,
    includeSelectAllOption: true
  });

  $("#whouse_id").multiselect({
    buttonWidth: '120px',
    nonSelectedText: '运往仓库',
    maxHeight: 200,
    includeSelectAllOption: true
  });

});