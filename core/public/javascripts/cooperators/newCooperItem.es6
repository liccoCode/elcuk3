$(() => {

  $("#addBtn").click(function () {
    let num = $("#scheme_table tr").length - 1;
    let params = {num: num};
    let html = _.template($('#add_template').html())(params)
    $("#scheme_table tbody").append(html)
  });

  $("#deleteBtn").click(function () {
    let num = $("#scheme_table tr").length - 1;
    if (num > 1) {
      $("#add_tr_" + num).remove()
    } else {
      noty({
        text: "最后一个方案无法删除！",
        type: 'error'
      });
    }
  });

  $("#search_btn").click(function (e) {
    e.preventDefault();
    let search = $("input[name='m.search']").val();
    let type = $("select[name='m.type']").val();
    $("#data_div").load($(this).data("url"), {
      search: search,
      type: type
    });
  });

  $("#bind_btn").click(function (e) {
    e.preventDefault();
    $("#bind_div").mask();
    let ids = [];
    let itemId = $(this).data("item");
    $("#data_div input[name='mids']:checked").each(function () {
      ids.push($(this).val());
    });
    $("#bind_div").load($(this).data("url"), {
      ids: ids,
      itemId: itemId
    }, function () {
      noty({
        text: "绑定成功！",
        type: 'success'
      });
      reloadBindDiv(itemId);
    });
  });

  $("#unBind_btn").click(function (e) {
    e.preventDefault();
    $("#bind_div").mask();
    let ids = [];
    let itemId = $(this).data("item");
    $("#bind_div input[name='mids']:checked").each(function () {
      ids.push($(this).val());
    });
    $("#bind_div").load($(this).data("url"), {
      ids: ids,
      itemId: itemId
    }, function () {
      noty({
        text: "解绑成功！",
        type: 'success'
      });
      reloadBindDiv(itemId);
    });
  });

  function reloadBindDiv (itemId) {
    $("#bind_div").load("/Cooperators/showMaterialForSku", {itemId: itemId});
    $("#bind_div").unmask();
  }

});