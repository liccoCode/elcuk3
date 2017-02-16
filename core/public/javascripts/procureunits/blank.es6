$(() => {

  //切换供应商, 自行寻找价格
  $("select[name='unit.cooperator.id']").change(function () {
    let id = $(this).val();
    if (id) {
      LoadMask.mask();
      $.post('/Cooperators/price', {
        id: id,
        sku: $('#unit_sku').val()
      }, function (r) {
        if (r.flag) {
          $("#unit_currency option:contains('#{r.currency}')").prop('selected', true);
          $("#unit_price").val(r.price);
          $("#unit_period").show();
          $("#unit_period").text('（生产周期：' + r['period'] + ' 天）')
          LoadMask.unmask()
        } else {
          noty({
            text: r.message,
            type: 'error'
          });
        }
      });
    } else {
      $("#unit_currency option:contains('CNY')").prop('selected', true)
      $("#unit_price").text('')
      $("#unit_period").hide()
    }
  });

  //快递同步预计到达时间

  $("[name='unit.attrs.planShipDate']").change(() => {
    let shipType = $("[name='unit.shipType']:checked").val();
    if (shipType != 'EXPRESS') {
      return;
    }
    let planShipDate = $("[name='unit.attrs.planShipDate']").val();
    let warehouseid = $("[name='unit.whouse.id']").val()
    $.get('/shipments/planArriveDate', {
      planShipDate: planShipDate,
      shipType: shipType,
      warehouseid
    }, function (r) {
      $("[name='unit.attrs.planArrivDate']").val(r['arrivedate']);
      showDay();
    });
  });

  $("#planQty,#availableQty").change(function () {
    if ($(this).val() < 0) {
      noty({
        text: "修改数值不能小于0",
        type: 'error'
      });
      let origin = $(this).data("origin");
      $(this).val(origin);
      return;
    }
    if ($(this).val() < $(this).data("origin") && $("#unit_type").val() != 'StockSplit') {
      $("#return_tr").show();
    } else {
      $("#return_tr").hide();
    }
  });

  $('#box_num').change(function (e) {
    e.preventDefault()
    let coperId = $("select[name='unit.cooperator.id']").val();
    if (coperId) {
      $.post('/cooperators/boxSize', {
        size: $('#box_num').val(),
        coperId: coperId,
        sku: $('#unit_sku').val()
      }, function (r) {
        if (r.flag) {
          $("input[name='unit.attrs.planQty']").val(r['message']);
        }
        else {
          noty({
            text: r.message,
            type: 'error'
          });
        }
      });
    }
    else {
      alert('请先选择 供应商');
    }
  });

  $("#warehouse_select").change(function () {
    if ($(this).val()) {
      let country = $("#warehouse_select :selected").text().split('_')[1];
      let sku = $("#unit_sku").val();
      $.get("/sellings/findSellingBySkuAndMarket", {
        sku: sku,
        market: "AMAZON_" + country
      }, function (c) {
        $("#sellingId").val(c);
        if (!$("#sellingId").val()) {
          noty({
            text: "市场对应无Selling",
            type: 'error'
          });
        } else {
          getCurrWhouse();
          getShipmentList();
        }
      });
    }
  });

  function getCurrWhouse () {
    let country = $("#warehouse_select :selected").text().split('_')[1];
    let shipType = $("input[name='unit.shipType']:checked").val();
    $.get("/whouses/autoMatching", {
      country: country,
      shipType: shipType
    }, function (r) {
      $('#curr_warehouse_select').find("option[value=" + r.id + "]").prop("selected", true);
    })
  }

  $("input[name='unit.shipType']").change(function () {
    let planDeliveryDate = $("input[name='unit.attrs.planDeliveryDate']").val();
    let whouseId = $("[name='unit.whouse.id']").val();
    if (planDeliveryDate && whouseId) {
      getShipmentList();
    }
  });

  //异步加载 Shipment
  function getShipmentList () {
    let whouseId = $("[name='unit.whouse.id']").val();
    let shipType = $("[name='unit.shipType']:checked").val();
    let planDeliveryDate = $("input[name='unit.attrs.planDeliveryDate']").val();
    let shipment = $("#shipments");
    if (!(planDeliveryDate && whouseId && shipType && shipment)) {
      noty({
        text: "请先填写【预计交货时间】【去往仓库】和【运输方式】！",
        type: 'error'
      });
      return false;
    }
    if (shipType == 'EXPRESS') {
      $('#shipments').html('因快递单情况变化很多, 快递单的选择由物流决定, 可不用选择快递单.');
      return;
    } else {
      LoadMask.mask(shipment)
    }
    $.post('/shipments/unitShipments', {
      whouseId: whouseId,
      shipType: shipType,
      planDeliveryDate: planDeliveryDate
    }, function (html) {
      shipment.html(html);
      LoadMask.unmask();
      if ($("#shipmentId").val()) {
        $("#shipments input[type='radio']").each(function () {
          if ($(this).val() == $("#shipmentId").val()) {
            $(this).prop("checked", true);
            showTrColor();
          }
        });
      }
    });

    if (shipType != 'EXPRESS') {
      return;
    }
    let planShipDate = $("[name='unit.attrs.planShipDate']").val();
    if (planShipDate == '') {
      return;
    }
    $.post('/shipments/planArriveDate', {
      planShipDate: planShipDate,
      shipType: shipType,
      warehouseid: whouseId
    }, function (r) {
      $("[name='unit.attrs.planArrivDate']").val(r['arrivedate']);
    });
  }

  $("#new_procure_unit [name='unit.product.sku']").on('change', function (e) {
    let $cooperators = $("select[name='unit.cooperator.id']")
    // 当输入的 sku 长度大于5才发起 Ajax 请求获取供应商列表
    if ($(this).val().length > 5) {
      LoadMask.mask();
      // Ajax 加载供应商列表
      $.post('/products/cooperators', {sku: this.value}, function (r) {
        alert(r);
        $cooperators.empty();
        $cooperators.append("<option value=''>请选择</option>");
        r.forEach(function (value) {
          $cooperators.append("<option value='#{value.id}'>value.name</option>");
          LoadMask.unmask();
        });
      });
    }
  });

  $('#shipments').on('change', '[name=shipmentId]', function () {
    LoadMask.mask();
    let shipmentId = $(this).val();
    $.get("/shipment/" + shipmentId + "/dates", "", function (r) {
      $("input[name='unit.attrs.planShipDate']").data('dateinput').setValue(r['begin']);
      $("input[name='unit.attrs.planArrivDate']").data('dateinput').setValue(r['end']);
      LoadMask.unmask();
      showTrColor();
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

  $("#create_unit").click(function (e) {
    e.preventDefault();
    if ($("#planQty").val() && $("input[name='unit.attrs.planDeliveryDate']").val()) {
      $("#new_procure_unit").submit();
    } else {
      noty({
        text: "请先填写采购数量和预计交货日期！",
        type: 'error'
      });
    }
  });

  $("[name='unit.attrs.planShipDate']").change(() => {
    let shipType = $("[name='unit.shipType']:checked").val();
    if (shipType != 'EXPRESS') {
      return;
    }
    let planShipDate = $("[name='unit.attrs.planShipDate']").val();
    let warehouseid = $("[name='unit.whouse.id']").val();
    $.get('/shipments/planArriveDate', {
      planShipDate: planShipDate,
      shipType: shipType,
      warehouseid: warehouseid
    }, function (r) {
      $("[name='unit.attrs.planArrivDate']").val(r['arrivedate']);
    });
  });
  // 计算时间到库日期与运输日期的差据
  $("[name='unit.attrs.planArrivDate']").change(() => {
    showDay();
  });

  showDay();

  function showDay () {
    let planShipDate = $("[name='unit.attrs.planShipDate']");
    let planArriveDate = $("[name='unit.attrs.planArrivDate']");
    if (planArriveDate.val() && planShipDate.val()) {
      planArriveDate.next().text((new Date(planArriveDate.val()) - new Date(planShipDate.val())) / (24 * 3600 * 1000) + "天");
    }
  }

  let unitId = window.location.hash.slice(1);
  let targetTr = $("#unit_" + unitId);
  if (targetTr.size() > 0) {
    EF.scoll(targetTr)
    EF.colorAnimate(targetTr)
  }

  $(document).ready(function () {
    let $shipType = $("[name='unit.shipType']");
    if ($shipType.val() !== void 0 && $shipType.val() !== 'EXPRESS' && $("#unitId").val()) {
      getShipmentList();
    }
  });

});