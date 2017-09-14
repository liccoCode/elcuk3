$(() => {

  // 切换供应商, 自行寻找价格
  $("select[name='dmt.cooperator.id']").change(function () {
    let id = $(this).val();
    if (id) {
      LoadMask.mask();
      $.get('/Cooperators/price', {
        id: id,
        sku: $('#unit_sku').val()
      }, function (r) {
        if (!r.flag) {
          alert(r.message);
        } else {
          $("select[name$='attrs.currency'] option:contains(" + r.currency + ")").prop('selected', true);
          $("#unit_price").val(r.price);
          $("#box_num").attr("boxSize", r.boxSize);
          calu_box_size();
        }
        LoadMask.unmask();
      });
    } else {
      $("#unit_currency option:contains('CNY')").prop('selected', true);
      $("#unit_price").val('');
    }
  });

  bind_b2b_checkbox();

  function bind_b2b_checkbox () {
    $('input[name$="isb2b"]').click(function () {
      if ($(this).prop("checked")) {
        $(this).parent().next().show();
      } else {
        $(this).parent().next().hide();
      }
    });
  }

  function calu_box_size () {
    $("input[name='box_size']").change(function () {
      let cooper_id = $("select[name='dmt.cooperator.id']").val();
      let boxSize = $(this).attr("boxSize");
      if (cooper_id && boxSize) {
        $(this).parent("div").prev("input").val(boxSize * $(this).val());
      } else {
        alert('请先选择 供应商!');
      }
    });
  }

  function sku_typeahead ($sku) {
    $sku.typeahead({
      source: (query, process) => {
        let sku = $sku.val();
        $.get('/products/sameSku', {
          sku: sku,
        }, function (c) {
          process(c);
        });
      },
      updater: (item) => {
        let $cooperators = $("select[name='dmt.cooperator.id']");
        $.get('/products/cooperators', {sku: item}, function (r) {
          LoadMask.mask();
          $cooperators.empty();
          $cooperators.append("<option value=''>请选择</option>");
          r.forEach(function (value) {
            $cooperators.append("<option value=" + value['id'] + ">" + value['name'] + "</option>");
          });
          LoadMask.unmask()
        });
        return item;
      }
    });
  }

  let $first_input_sku = $("#unit_sku");
  sku_typeahead($first_input_sku);

  function init () {
    let $sku = $("input[name$='product.sku']:not(:first)");
    let cooperId = $("select[name='dmt.cooperator.id']").val();
    $sku.each(function (index) {
      let $input = $(this);
      $input.typeahead({
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
          $.get('/Cooperators/price', {
            id: cooperId,
            sku: item
          }, function (r) {
            if (!r.flag) {
              alert(r.message);
            } else {
              $input.parent("div").parent("div").find("input[name$='attrs.price']").val(r.price);
              $input.parent("div").parent("div").parent("div[class='box-body']").find("input[name='box_size']").attr("boxSize", r.boxSize);
              $input.parent("div").parent("div").find("option:contains(" + r.currency + ")").prop('selected', true);
              calu_box_size();
            }
            LoadMask.unmask();
          });
          return item;
        }
      });
    });
  }

  $("#addSkuBtn").click(function (e) {
    e.preventDefault();
    if (!$("select[name='dmt.cooperator.id']").val()) {
      alert("请先选择 供应商！");
      return;
    }
    let index = $("input[name$='product.sku']").length;
    let html = _.template($("#copy").text())({"num": index});
    $("#new_procureunit").append(html);
    window.$dataui.dateinput();
    init();
    validQty();
    bind_b2b_checkbox();
    addDeleteBtn();
  });

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

  function addDeleteBtn () {
    $("[name='delete_btn']").remove();
    let $btn = "<div class='col-sm-3' style='text-align:right;'>";
    $btn += "<button type='button' class='btn btn-danger' name='delete_btn'>删除</button></div>";
    let length = $("div[class='box-body']").length;
    if (length > 1) {
      $("#tail-form-" + (length - 1)).append($btn);
    }
  }

  $(document).on("click", "[name='delete_btn']", function (e) {
    let length = $("div[class='box-body']").length;
    $("#box-" + (length - 1)).remove();
  });

  validQty();

});