$(() => {


  function fidCallBack () {
    return {
      fid: $('#p_sku').val(),
      p: 'SKU'
    }
  }

  let dropbox = $('#dropbox');
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1');
  window.dropUpload.iniDropbox(fidCallBack, dropbox);


  $("#update_product_form").on("click", "#more_locate_btn, #more_selling_point_btn", function () {
    let div = $("#" + $(this).data("table"));
    let rowsCount = div.find(".form-group").length;
    let cloneDiv = div.find(".form-group")[rowsCount - 1];
    let newDiv = cloneDiv.cloneNode(true);
    let textareas = newDiv.getElementsByTagName("textarea");
    setTextAreaName($(this).attr("id"), rowsCount, textareas);
    $("#" + $(this).data("table")).append(newDiv);
  }).on("click", "[name^='delete_locate_row'], [name^='delete_selling_point_row']", function () {
    let $btn = $(this);
    // remove 掉按钮所在的那一行
    $btn.parent("div").parent().remove()
  });

  function setTextAreaName (flag, rowsCount, textareas) {
    if (flag == "more_locate_btn") {
      textareas[0].name = "pro.locate[" + rowsCount + "].title";
      textareas[0].value = "";
      textareas[1].name = "pro.locate[" + rowsCount + "].content";
      textareas[1].value = "";
    } else {
      textareas[0].name = "pro.sellingPoint[" + rowsCount + "].title";
      textareas[0].value = "";
      textareas[1].name = "pro.sellingPoint[" + rowsCount + "].content";
      textareas[1].value = "";
    }
  }

  $("#basicinfo").on("click", "#save_basic_btn", function () {
    if (!validUpcAndPartNumber()) {
      return;
    }
    if ($('input[name="pro.iscopy"]').val() == "2") {
      if (!confirm('该SKU的产品名称与选择的SKU的产品名称一致,确定保存?')) {
        return;
      }
    }
    if (!$("#proabbreviation").val()) {
      noty({
        text: "产品名称不允许为空.",
        type: 'error',
        timeout: 5000
      });
      return;
    }
    LoadMask.mask();
    $.post("/products/update", $("#update_product_form").serialize(), function (r) {
      if (r.flag) {
        noty({
          text: "保存成功.",
          type: 'success',
          timeout: 5000
        });
      } else {
        noty({
          text: "#{r.message}",
          type: 'error',
          timeout: 5000
        });
      }
      LoadMask.unmask();
    });
  }).on("change", "#proabbreviation", function () {
    $('input[name="pro.iscopy"]').val("1");
  });

  $(document).on("change", "input[name='pro.weight'], input[name='pro.productWeight']", function () {
    //将输入的重量(kg)换算成盎司(oz)
    let $input = $(this);
    if ($input.val() !== "" && $input.val() > 0 && !isNaN($input.val())) {
      $input.next().next().val(($input.val() * 35.2739619).toFixed(2));
    }
  }).on("change", "input[name='pro.lengths'], input[name='pro.width'], input[name='pro.heigh'], input[name='pro.productLengths'], input[name='pro.productWidth'], input[name='pro.productHeigh']", function () {
    //将输入的长度、宽度、高度换算成英寸(inch)
    let $input = $(this);
    if ($input.val() !== "" && $input.val() > 0 && !isNaN($input.val())) {
      $input.next().next().val(($input.val() * 0.0393701).toFixed(2));
    }
  });

  $("#deleteBtn").click(function () {
    let $deleteBtn = $(this);
    let $logForm = $("#logForm");
    $("#delete_sku_input").val($deleteBtn.data('sku'));
    $logForm.modal();
  });

  // 页面初始化时触发一次
  let inputs = ["input[name='pro.lengths']", "input[name='pro.width']", "input[name='pro.heigh']",
    "input[name='pro.productLengths']", "input[name='pro.productWidth']", "input[name='pro.productHeigh']",
    "input[name='pro.weight']", "input[name='pro.productWeight']"];
  _.each(inputs, function (value) {
    $(value).trigger("change");
  });

  function validUpcAndPartNumber () {
    let flag = true
    if (!$("#upc").val()) {
      noty({
        text: "UPC必须填写.",
        type: 'error',
        timeout: 5000
      });
      flag = false;
    }
    if (!$("#partNumber").val()) {
      noty({
        text: "Part Number必须填写.",
        type: 'error',
        timeout: 5000
      });
      flag = false;
    }
    return flag;
  }

  $("#search_btn").click(function (e) {
    e.preventDefault();
    let search = $("input[name='m.search']").val();
    let type = $("input[name='m.type']").val();
    $("#data_div").load($(this).data("url"), {
      search: search,
      type: type
    });
  });

  $("#bind_btn").click(function (e) {
    e.preventDefault();
    let ids = [];
    let sku = $(this).data("sku");
    $("#data_div input[name='mids']:checked").each(function () {
      ids.push($(this).val());
    });
    $("#bind_div").load($(this).data("url"), {
      ids: ids,
      sku: sku
    }, function () {
      noty({
        text: "绑定成功！",
        type: 'success'
      });
    });
  });

  $("#unBind_btn").click(function (e) {
    e.preventDefault();
    let ids = [];
    let sku = $(this).data("sku");
    $("#bind_div input[name='mids']:checked").each(function () {
      ids.push($(this).val());
    });
    $("#bind_div").load($(this).data("url"), {
      ids: ids,
      sku: sku
    }, function () {
      noty({
        text: "解绑成功！",
        type: 'success'
      });
    });
  });

  $("input[name='input_attr']").typeahead({
    source: (query, process) => {
      let name = $("input[name='input_attr']");
      $.get(name.data("url"), {name: name.val()}, function (c) {
        process(c);
      });
    }
  });

  $("#add_attr_btn").click(function () {
    $("#commit_attr").val($("input[name='input_attr']").val());
    $("#add_attr_form").submit();
  });

  $("#save_attrs_btn").click(function () {
    LoadMask.mask();
    let form = $("#save_attrs_form");
    $.post("/products/saveAttrs", form.serialize() + "&hsCode=" + $("input[name='hs_code']").val(), function (r) {
      let msg;
      if (r.flag) {
        msg = {
          text: "保存成功",
          type: 'success',
          timeout: 2000
        }
      } else {
        msg = {
          text: r.message,
          type: 'error',
          timeout: 2000
        }
      }
      noty(msg);
      LoadMask.unmask();
    });
  });

  $("#sync_sku_btn").click(function () {
    let sku = $("#syncSku").val();
    if (!sku) {
      noty({
        text: "请选择要同步的SKU",
        type: 'error',
        timeout: 3000
      });
    } else {
      $("#syncSkuInput").val(sku);
      LoadMask.mask();
      $("#extends_atts_home").load("/Products/syncSku", $("#add_attr_form").serialize(), function (r) {

        LoadMask.unmask();
        let msg = {
          text: "同步成功",
          type: 'success',
          timeout: 2000
        };
        noty(msg);
      });
    }
  });

  $("#extends").on("click", "#add_template_btn", function (e) {
    let temp_id = $("select[name='templateIdSelect']").val();
    if (!temp_id) {
      noty({
        text: "请选择要加载的模板",
        type: 'error',
        timeout: 5000
      });
    } else {
      $("#commit_templateId").val(temp_id);
      e.preventDefault();
      $("#extends_atts_home").load("/Products/attrs", $("#add_attr_form").serialize(), function (r) {
        LoadMask.unmask()
      });
    }
  });

  if ("attr" == $("#attrVal").val()) {
    $("#attrBtn").click();
  }
  
  $("#whouseAttrs").on("click", "#save_whouse_atts_btn", function () {
    LoadMask.mask();
    $.post("/products/update", $("#whouse_attrs_form").serialize(), function (r) {
      if (r.flag) {
        noty({
          text: "保存成功.",
          type: 'success',
          timeout: 5000
        });
      } else {
        noty({
          text: "#{r.message}",
          type: 'error',
          timeout: 5000
        });
      }
      LoadMask.unmask();
    });
  }).on("click", "#whouse_attrs_attach_btn", function () {
    $file_home = $('#file_home');
    $.post("/products/update", {
      p: 'PRODUCTWHOUSE',
      fid: $file_home.data('fid'),
      base64File: $file_home.data('base64_file'),
      originName: $file_home.data('origin_name')
    }, function (r) {
      alert(r.message);
      window.location.reload();
    });
  });
});

