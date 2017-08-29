$(() => {

  $("#create_product_form").on("click", "#more_locate_btn, #more_selling_point_btn", function () {
    let div = $("#" + $(this).data("table"));
    let rowsCount = div.find(".form-group").length;
    let cloneDiv = div.find(".form-group")[rowsCount - 1];
    let newDiv = cloneDiv.cloneNode(true);
    let textareas = newDiv.getElementsByTagName("textarea");
    setTextAreaName($(this).attr("id"), rowsCount, textareas);
    $("#" + $(this).data("table")).append(newDiv);
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

});