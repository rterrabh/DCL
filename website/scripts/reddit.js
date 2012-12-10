





(function() {
       
    var styled_submit = '<a style="color: #369; text-decoration: none;" href="http://www.reddit.com/submit?url=http%3A%2F%2Fdclsuite.org%2F&title=" target="_new">';
    var unstyled_submit = '<a href="http://www.reddit.com/submit?url=http%3A%2F%2Fdclsuite.org%2F&title=;title=" target="http://www.reddit.com/submit?url=http%3A%2F%2Fdclsuite.org%2F&title=">';
    var write_string='<span class="reddit_button" style="';
    write_string += 'color: grey;';
    write_string += '">';
    write_string += unstyled_submit + '<img style="height: 2.3ex; vertical-align:top; margin-right: 1ex" src="http://www.redditstatic.com/spreddit2.gif">' + "</a>";
    write_string += '4 points';
        write_string += ' on ' + styled_submit + 'reddit</a>';
    write_string += '</span>';

document.write(write_string);
})()
