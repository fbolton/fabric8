(function() {
	/**
	 * provides text formatting and i18n key storage features<br>
	 * implements most of the Sun Java MessageFormat functionality.
	 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/text/MessageFormat.html" target="sun">Sun's Documentation</a>
	 */

	var keys = {};

	var format = function(message, args) {
		var substitute = function() {
			var format = arguments[1].split(',');
			var substr = escape(args[format.shift()]);
			if(format.length === 0) {
				return substr; // simple substitution eg {0}
			}
			switch(format.shift()) {
				case "number" : return (new Number(substr)).toLocaleString();
				case "date" : return (new Date(+substr)).toLocaleDateString(); // date and time require milliseconds since epoch
				case "time" : return (new Date(+substr)).toLocaleTimeString(); //  eg i18n.text("Key", +(new Date())); for current time
			}
			var styles = format.join("").split("|").map(function(style) {
				return style.match(/(-?[\.\d]+)(#|<)([^{}]*)/);
			});
			var match = styles[0][3];
			for(var i=0; i<styles.length; i++) {
				if((styles[i][2] === "#" && (+styles[i][1]) === (+substr)) ||
						(styles[i][2] === "<" && ((+styles[i][1]) < (+substr)))) {
					match = styles[i][3];
				}
			}
			return match;
		};

		return message && message.replace(/'(')|'([^']+)'|([^{']+)|([^']+)/g, function(x, sq, qs, ss, sub) {
			do {} while(sub && (sub !== (sub = (sub.replace(/\{([^{}]+)\}/, substitute)))));
			return sq || qs || ss || unescape(sub);
		});
	};

	this.i18n = {

		setKeys: function(strings) {
			for(var key in strings) {
				keys[key] = strings[key];
			}
		},

		text: function() {
			var args = Array.prototype.slice.call(arguments),
				key = keys[args.shift()];
			if(args.length === 0) {
				return key;
			}
			return format(key, args);
		},

		complex: function() {
			var args = Array.prototype.slice.call(arguments),
				key = keys[args.shift()],
				ret = [];
			do {} while(key && key !== (key = key.replace(/([^{]+)|\{(\d+)\}/, function(x, pt, sub) {
				ret.push(pt || args[+sub]);
				return "";
			})));
			return ret;
		}

	};

})();
