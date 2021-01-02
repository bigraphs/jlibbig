$(function () {
    $('.js-copy.js-tooltip, .js-download.js-tooltip').popover({template:'<div class="popover" role="tooltip"><div class="arrow"></div><div class="popover-body"></div></div>',title:'',placement: 'top'});
    if (document.queryCommandSupported('copy') === true) {
        $('.js-copy').click(function () {
            var el = $(this);
            if (el.is('[data-src]')) {
                copyToClipboard($(el.data('src')).text(), el);
            } else if (el.is('[data-copy]')) {
                copyToClipboard(el.data('copy'), el);
            } else {
                console.log('Nothing to copy');
            }
        });
    } else {
        $('.js-copy').remove();
    };
    if (encodeURIComponent) {
        $('.js-download').click(function () {
            var el = $(this);
            if (el.is('[data-src]')) {
                download($(el.data('src')).text(), el.data('type'), el.data('filename'), el);
            } else if (el.is('[data-copy]')) {
                download(el.data('copy'), el.data('type'), el.data('filename'), el);
            } else {
                console.log('Nothing to download');
            }
        });
    } else {
        $('.js-download').remove();
    };
});
function copyToClipboard(text, el) {
    const elOriginalText = el.attr('data-content');
    el.attr('data-content', '<i class="fas fa-spinner"></i> Copying...').popover('show');
    var msg = '<i class="fas fa-exclamation-circle"></i> Woops, copy failed!';
    var copyTextArea;
    try {
        copyTextArea = document.createElement("textarea");
        copyTextArea.value = text;
        document.body.appendChild(copyTextArea);
        copyTextArea.select();
        if (document.execCommand('copy')) { msg = '<i class="fas fa-clipboard-check"></i> Copied to clipboard!'; }
    } catch (err) {
        console.log('Unable to copy: ' + err);
    } finally {
        el.attr('data-content', msg).popover('show');
        setTimeout(function () { el.popover('hide'); }, 2000);
        document.body.removeChild(copyTextArea);
        el.attr('data-content', elOriginalText);
    }
}
function download(data, type, filename, el) {
    const elOriginalText = el.attr('data-content');
    el.attr('data-content', '<i class="fas fa-spinner"></i> Starting...').popover('show');
    var msg = '<i class="fas fa-exclamation-circle"></i> Woops, download failed!';
    var a;
    try {
        a = document.createElement('a');
        a.setAttribute('href', 'data:' + type + ', ' + encodeURIComponent(data));
        a.setAttribute('download', filename);
        document.body.appendChild(a);
        a.click();
        msg = '<i class="fas fa-check-circle"></i> Download started!';
    } catch (err) {
        console.log('Unable to download: ' + err);
    } finally {
        el.attr('data-content', msg).popover('show');
        setTimeout(function () { el.popover('hide'); }, 2000);
        document.body.removeChild(a);
        el.attr('data-content', elOriginalText);
    }
}