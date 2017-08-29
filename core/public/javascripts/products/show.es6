$(() => {

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
});