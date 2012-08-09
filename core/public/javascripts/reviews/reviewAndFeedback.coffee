$ ->
# 使用 Ticket Tag 的 html 代码例子
#<div class="row-fluid" id="reason_div">
#  <div class="span5">
#  <!-- 结合 tag 使用的 -->
#    <input type="hidden" id="ticketId" value="${review.ticket.id}">
#    <span class="reason badge badge-important">${ra.name()}</span>
#   </div>
#  <div class="span7 input-append">
#    <span class="unTagReason badge badge-info">${ura.name()}</span>
#  </div>
#</div>
  ticketId = -> $('#ticketId').val().trim()

  # 取消某一个 Ticket 的原因
  reasonToUnTagReason = (span) ->
    o = $(span)
    mask = $('#reason_div')
    mask.mask('取消原因中...')
    $.post('/reviews/unTagReason', {reason: o.html(), ticketId: ticketId()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          o.removeClass('reason')
          .removeClass('badge-important')
          .addClass('unTagReason')
          .addClass('badge-info')
          .unbind().click -> unTagReasonToReason(@)
          $("#reason_div div:eq(1)").append(o)
        mask.unmask()
    )

  # 为某一个 Ticket 添加原因
  unTagReasonToReason = (span) ->
    o = $(span)
    mask = $('#reason_div')
    mask.mask('添加原因中...')
    $.post('/reviews/tagReason', {reason: o.html(), ticketId: ticketId()},
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          o.removeClass('unTagReason')
          .removeClass('badge-info')
          .addClass('reason')
          .addClass('badge-important')
          .unbind().click -> reasonToUnTagReason(@)
          $("#reason_div div:eq(0)").append(o)
        mask.unmask()
    )


  # 为此 Listing 添加 Tag
  $('span.reason').css('cursor', 'pointer').click -> reasonToUnTagReason(@)
  $('span.unTagReason').css('cursor', 'pointer').click -> unTagReasonToReason(@)