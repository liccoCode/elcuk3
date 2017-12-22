$(() => {
  //Ajax 加载 Shipment
  $('input[name="newUnit.shipType"],input[name="newUnit.attrs.planDeliveryDate"]').change(function () {
    if ($("#splitType").val() != "false") {
      getShipmentList();
    }
  });

  $('#shipments').on('change', '[name=shipmentId]', function () {
    LoadMask.mask();
    let shipmentId = $(this).val();
    $.get("/shipment/" + shipmentId + "/dates", "", function (r) {
      $("input[name='newUnit.attrs.planShipDate']").datepicker('setDate', r['begin']);
      $("input[name='newUnit.attrs.planArrivDate']").datepicker('setDate', r['end']);
      showTrColor();
      LoadMask.unmask();
    });
  });

  function showTrColor () {
    $("#shipments input[type='radio']").each(function () {
      if ($(this).prop("checked")) {
        $(this).parent("td").parent("tr").attr("style", "background-color:#F2DEDE;");
      } else {
        $(this).parent("td").parent("tr").attr("style", "");
      }
    });
  }

  let $sku = $("#select_sku");
  let cooperId = $("#cooperId").val();
  $sku.typeahead({
    source: (query, process) => {
      let sku = $sku.val();
      $.get('/products/sameSkuAndCooper', {
        sku: sku,
        cooperId: cooperId
      }, function (c) {
        process(c);
      });
    },
    updater: (item) => {
      getSelling(item);
      getProductName(item);
      refreshTaxPrice(item);
      return item;
    }
  });

  getSelling($sku.val());
  showDay();

  $("#warehouse_select").change(function () {
    if ($(this).val()) {
      let country = $("#warehouse_select :selected").text().split('_')[1];
      let sku = $("#select_sku").val();
      $.get("/sellings/findSellingBySkuAndMarket", {
        sku: sku,
        market: "AMAZON_" + country,
        id: $("#warehouse_select").val()
      }, function (c) {
        $("#sellingId").val(c);
        if ($("#sellingId").val()) {
          getShipmentList();
        } else {
          noty({
            text: "市场对应无Selling",
            type: 'error'
          });
          $("#warehouse_select").prop("value", "");
        }
      });
    } else {
      $("#sellingId").val("");
      $("input[name='newUnit.shipType']").each(function () {
        $(this).prop("checked", false);
      });
    }
  });

  //通过SKU 和 供应商获取相关信息
  function getProductName (sku) {
    let cooperId = $("#cooperId").val();
    let containTax = $("select[name='unit.containTax']").val();
    $.get("/Cooperators/findTaxPrice", {
      sku: sku,
      cooperId: cooperId,
      containTax: containTax
    }, function (r) {
      $("#productName").val(r.name);
      $("#price_input").val(r['price']);
      $("#unit_currency").prop("value", r['currency']);
      $("#unit_period").html("(生产周期：" + r['period'] + "天)");
      let text = containTax == "true" ? ("税点：" + r['taxPoint']) : "";
      $("#taxSpan").text(text);
      $("#taxPointInput").val(r['taxPoint']);
      if ($("#stage").val() != 'IN_STORAGE') {
        $("#size_of_box").val(r['boxSize']);
      }
    });
  }

  function getSelling (sku) {
    if (sku && $("#warehouse_select").val()) {
      let country = $("#warehouse_select :selected").text().split('_')[1];
      $.get("/sellings/findSellingBySkuAndMarket", {
        sku: sku,
        market: "AMAZON_" + country,
        id: $("#warehouse_select").val()
      }, function (r) {
        $("#sellingId").val(r);
        if (!$("#sellingId").val()) {
          noty({
            text: "市场对应无Selling",
            type: 'error'
          });
          $("#warehouse_select").prop("value", "");
        }
      });
    }
  }

  function getShipmentList () {
    let whouseId = $("[name='newUnit.whouse.id']").val();
    let shipType = $("[name='newUnit.shipType']:checked").val();
    let planDeliveryDate = $("[name='newUnit.attrs.planDeliveryDate']").val();
    let shipment = $("#shipments");
    if (planDeliveryDate && whouseId && shipType && shipment) {
      if (shipType === 'EXPRESS' || shipType === 'DEDICATED') {
        $('#shipments').html('因快递单情况变化很多, 快递单的选择由物流决定, 可不用选择快递单.');
      } else {
        LoadMask.mask();
        $.get('/shipments/unitShipments', {
          whouseId: whouseId,
          shipType: shipType,
          planDeliveryDate: planDeliveryDate
        }, function (html) {
          shipment.html(html);
          LoadMask.unmask();
        });
      }
    } else {
      noty({
        text: "请先填写【预计交货时间】【去往仓库】和【运输方式】！",
        type: 'error'
      });
    }
  }

  $('#box_num').change(function () {
    let boxSize = $("#size_of_box").val();
    let boxNum = $("#box_num").val();
    if (boxSize * boxNum > $("#planQty").data('max')) {
      noty({
        text: "分拆数量不能大于" + $("#planQty").data('max'),
        type: 'error'
      });
      $("#planQty").val(0);
    } else {
      $("#planQty").val(boxSize * boxNum);
      showBoxInfo();
    }
  });

  $("#planQty").change(function () {
    if ($(this).val() > $(this).data('max')) {
      noty({
        text: "分拆数量不能大于" + $(this).data('max'),
        type: 'error'
      });
      $(this).val(0);
    }
    if ($(this).val() < 0) {
      noty({
        text: "分拆数量不能小于0",
        type: 'error'
      });
      $(this).val(0);
    }
  });

  $("input[name='newUnit.availableQty']").change(function () {
    showBoxInfo();
  });

  //快递同步预计到达时间
  $("[name='newUnit.attrs.planShipDate']").change(() => {
    let shipType = $("[name='newUnit.shipType']:checked").val();
    if (shipType != 'EXPRESS' && shipType != 'DEDICATED') {
      return;
    }
    let planShipDate = $("[name='newUnit.attrs.planShipDate']").val();
    let warehouseid = $("[name='newUnit.whouse.id']").val();
    $.get('/shipments/planArriveDate', {
      planShipDate: planShipDate,
      shipType: shipType,
      warehouseid: warehouseid
    }, function (r) {
      $("[name='newUnit.attrs.planArrivDate']").val(r['arrivedate']);
      showDay();
    });
  });

  function showBoxInfo () {
    let qty = $("input[name='newUnit.availableQty']").val();
    let size = $("#size_of_box").val();
    let lastNum = $("#lastBoxNum").val();
    let lastBoxNum = qty % size == 0 ? 0 : 1;
    $("#boxInfo").text("(主箱数:" + parseInt(qty / size) + " 每箱个数:" + size + " 尾箱数:" + lastBoxNum + " 每箱个数:" + qty % size + ")");
    qty % size > lastNum ? $("#warningText").show() : $("#warningText").hide();
  }

  $("[name='newUnit.attrs.planArrivDate']").change(() => {
    showDay();
  });

  function showDay () {
    let planShipDate = $("[name='newUnit.attrs.planShipDate']");
    let planArriveDate = $("[name='newUnit.attrs.planArrivDate']");
    if (planArriveDate.val() && planShipDate.val()) {
      planArriveDate.next().text((new Date(planArriveDate.val()) - new Date(planShipDate.val())) / (24 * 3600 * 1000) + "天");
    }
  }

  $("select[name='newUnit.containTax']").change(function () {
    refreshTaxPrice($('#select_sku').val());
  });

  function refreshTaxPrice (sku) {
    let cooperId = $("#cooperId").val();
    let containTax = $("select[name='newUnit.containTax']").val();
    LoadMask.mask();
    $.post("/Cooperators/findTaxPrice", {
      cooperId: cooperId,
      sku: sku,
      containTax: containTax
    }, function (r) {
      if (!r.flag) {
        alert(r.message);
      } else {
        $("select[name$='attrs.currency'] option:contains(" + r.currency + ")").prop('selected', true);
        $("#price_input").val(r.price);
        $("#size_of_box").attr("boxSize", r.boxSize);
        let text = containTax == "true" ? ("税点：" + r.taxPoint) : "";
        $("#taxPointInput").val(r['taxPoint']);
        $("#taxSpan").text(text);
      }
      LoadMask.unmask();
    });
  }

});