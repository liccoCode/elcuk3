$(() => {

  let selectLet;

  // 切换物料名称, 自行查询物料编码
  $("#unit_material").change(function () {
    let id = $(this).val();
    if (id) {
      LoadMask.mask();
      $.get('/Materials/findMaterial', {
        id: id
      }, function (r) {
        //物料code赋值
        $("#materialCode").val(r.code);
        //级联查询供应商
        let $cooperators = $("select[name='purchase.cooperator.id']");

        $.get('/MaterialPurchases/cooperators', {id: id}, function (r) {
          LoadMask.mask();
          $cooperators.empty();
          $cooperators.append("<option value=''>请选择</option>");
          r.forEach(function (value) {
            $cooperators.append("<option value=" + value['id'] + ">" + value['name'] + "</option>");
          });
          LoadMask.unmask()
        });

        LoadMask.unmask();
      });
    }
  });

  // 切换供应商, 自行寻找价格
  $("select[name='purchase.cooperator.id']").change(function () {
    let id = $(this).val();
    let materialId = $("#unit_material").val();
    if (id) {
      LoadMask.mask();
      // 1 寻找价格
      $.get('/MaterialPurchases/price', {
        cooperId: id,
        materialId: materialId
      }, function (r) {
        if (!r.flag) {
          alert(r.message);
        } else {
          $("select[name$='attrs.currency'] option:contains(" + r.currency + ")").prop('selected', true);
          $("#unit_price").val(r.price);
          $("#box_num").attr("boxSize", r.boxSize);
          calu_box_size();
        }
      });

      // 2 根据供应商 反向查询该供应商对应的 物料信息
      $.get('/MaterialPurchases/materials', {
        cooperId: id
      }, function (c) {
        let html = "  <select name='units[<%= num %>].material.id' class='inline selectize' style='width:150px;'> ";
        html += "<option value=''>请选择</option>";
        c.forEach(function (value) {
          html += "<option value=" + value['id'] + ">" + value['name'] + "</option>";
        });
        html += "</select>";
        console.log(html);
        selectLet = html;

      });
      LoadMask.unmask();
    } else {
      $("#unit_currency option:contains('CNY')").prop('selected', true);
      $("#unit_price").val('');
    }
  });

  function calu_box_size () {
    $("input[name='box_size']").change(function () {
      let cooper_id = $("select[name='purchase.cooperator.id']").val();
      let boxSize = $(this).attr("boxSize");
      if (cooper_id && boxSize) {
        $(this).prev("input").val(boxSize * $(this).val());
      } else {
        alert('请先选择 供应商!');
      }
    });
  }

  $("#addSkuBtn").click(function (e) {
    e.preventDefault();
    if (!$("select[name='purchase.cooperator.id']").val()) {
      alert("请先选择 供应商！");
      return;
    }
    let index = $("select[name$='material.id']").length;
    let html = _.template($("#copy").text())({"num": index});
    html = html.replace("<select></select>", selectLet);
    html = html.replace("units[<%= num %>].material.id", "units["+index+"].material.id");
    html = html.replace("units[&lt;%= num %&gt;].currency", "units["+index+"].currency");
    console.log(html);
    $("#btn_tr").before(html);
    window.$ui.dateinput();
    init();
    validQty();
    bind_b2b_checkbox();
  });

  
  function init () {
    // 动态绑定事件   新增的明细 切换物料名称  寻找价格
    $("select[name$='material.id']:not(:first)").change(function () {
      let $input = $(this);
      let cooperId = $("select[name='purchase.cooperator.id']").val();
      let id = $(this).val();

      $.get('/Materials/findMaterial', {
              id: id
            }, function (r) {
              //物料code赋值
              $input.parent("td").parent("tr").find("input[name$='material.code']").val(r.code);

            });

      $.get('/MaterialPurchases/price', {
        cooperId: cooperId,
        materialId: id
      }, function (r) {
        if (!r.flag) {
          alert(r.message);
        } else {
          $input.parent("td").parent("tr").next("tr").find("input[name$='price']").val(r.price);
          $input.parent("td").parent("tr").next("tr").find("input[name='box_size']").attr("boxSize", r.boxSize);
          $input.parent("td").parent("tr").next("tr").find("option:contains(" + r.currency + ")").prop('selected', true);
          calu_box_size();
        }
      });


    });
  }

  function validQty () {
    $("input[name$='attrs.planQty'],input[name$='availableQty']").change(function () {
      if ($(this).val() < 0) {
        noty({
          text: '数量不能小于0！',
          type: 'error'
        });
        $(this).val(0);
      }
    });
  }

  validQty();

  function bind_b2b_checkbox () {
    $('input[name$="isb2b"]').click(function () {
      if ($(this).prop("checked")) {
        $(this).parent().next().show();
      } else {
        $(this).parent().next().hide();
      }
    });
  }

});