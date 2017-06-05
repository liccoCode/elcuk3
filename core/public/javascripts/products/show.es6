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

});