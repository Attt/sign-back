
function getPass(pass,serverTime){
    var password = _SBCtoDBC(pass) + serverTime;
    setMaxDigits(131);
    var g = new RSAKeyPair("10001","","B3C61EBBA4659C4CE3639287EE871F1F48F7930EA977991C7AFE3CC442FEA49643212E7D570C853F368065CC57A2014666DA8AE7D493FD47D171C0D894EEE3ED7F99F6798B7FFD7B5873227038AD23E3197631A8CB642213B9F27D4901AB0D92BFA27542AE890855396ED92775255C977F5C302F1E7ED4B1E369C12CB6B1822F");
    // return g.toString();
    return encryptedString(g, password)
}

 function _SBCtoDBC(t) {
    var e = "";
    if (t) {
        for (var i = t.length, n = 0; i > n; n++) {
            var a = t.charCodeAt(n);
            a = a >= 65281 && 65374 >= a ? a - 65248 : a,
                a = 12288 == a ? 32 : a,
                e += String.fromCharCode(a)
        }
        return e
    }
}

function BarrettMu(t) {
    this.modulus = biCopy(t),
        this.k = biHighIndex(this.modulus) + 1;
    var e = new BigInt;
    e.digits[2 * this.k] = 1,
        this.mu = biDivide(e, this.modulus),
        this.bkplus1 = new BigInt,
        this.bkplus1.digits[this.k + 1] = 1,
        this.modulo = BarrettMu_modulo,
        this.multiplyMod = BarrettMu_multiplyMod,
        this.powMod = BarrettMu_powMod
}

function BarrettMu_modulo(t) {
    var e = biDivideByRadixPower(t, this.k - 1)
        , i = biMultiply(e, this.mu)
        , n = biDivideByRadixPower(i, this.k + 1)
        , a = biModuloByRadixPower(t, this.k + 1)
        , o = biMultiply(n, this.modulus)
        , s = biModuloByRadixPower(o, this.k + 1)
        , r = biSubtract(a, s);
    r.isNeg && (r = biAdd(r, this.bkplus1));
    for (var c = biCompare(r, this.modulus) >= 0; c;)
        r = biSubtract(r, this.modulus),
            c = biCompare(r, this.modulus) >= 0;
    return r
}

function BarrettMu_multiplyMod(t, e) {
    var i = biMultiply(t, e);
    return this.modulo(i)
}

function BarrettMu_powMod(t, e) {
    var i = new BigInt;
    i.digits[0] = 1;
    for (var n = t, a = e; 0 != (1 & a.digits[0]) && (i = this.multiplyMod(i, n)),
        a = biShiftRight(a, 1),
    0 != a.digits[0] || 0 != biHighIndex(a);)
        n = this.multiplyMod(n, n);
    return i
}

function setMaxDigits(t) {
    maxDigits = t,
        ZERO_ARRAY = new Array(maxDigits);
    for (var e = 0; e < ZERO_ARRAY.length; e++)
        ZERO_ARRAY[e] = 0;
    bigZero = new BigInt,
        bigOne = new BigInt,
        bigOne.digits[0] = 1
}

function BigInt(t) {
    this.digits = "boolean" == typeof t && 1 == t ? null : ZERO_ARRAY.slice(0),
        this.isNeg = !1
}

function biFromDecimal(t) {
    for (var e, i = "-" == t.charAt(0), n = i ? 1 : 0; n < t.length && "0" == t.charAt(n);)
        ++n;
    if (n == t.length)
        e = new BigInt;
    else {
        var a = t.length - n
            , o = a % dpl10;
        for (0 == o && (o = dpl10),
                 e = biFromNumber(Number(t.substr(n, o))),
                 n += o; n < t.length;)
            e = biAdd(biMultiply(e, lr10), biFromNumber(Number(t.substr(n, dpl10)))),
                n += dpl10;
        e.isNeg = i
    }
    return e
}

function biCopy(t) {
    var e = new BigInt(!0);
    return e.digits = t.digits.slice(0),
        e.isNeg = t.isNeg,
        e
}

function biFromNumber(t) {
    var e = new BigInt;
    e.isNeg = 0 > t,
        t = Math.abs(t);
    for (var i = 0; t > 0;)
        e.digits[i++] = t & maxDigitVal,
            t >>= biRadixBits;
    return e
}

function reverseStr(t) {
    for (var e = "", i = t.length - 1; i > -1; --i)
        e += t.charAt(i);
    return e
}

function biToString(t, e) {
    var i = new BigInt;
    i.digits[0] = e;
    for (var n = biDivideModulo(t, i), a = hexatrigesimalToChar[n[1].digits[0]]; 1 == biCompare(n[0], bigZero);)
        n = biDivideModulo(n[0], i),
            digit = n[1].digits[0],
            a += hexatrigesimalToChar[n[1].digits[0]];
    return (t.isNeg ? "-" : "") + reverseStr(a)
}



function biToDecimal(t) {
    var e = new BigInt;
    e.digits[0] = 10;
    for (var i = biDivideModulo(t, e), n = String(i[1].digits[0]); 1 == biCompare(i[0], bigZero);)
        i = biDivideModulo(i[0], e),
            n += String(i[1].digits[0]);
    return (t.isNeg ? "-" : "") + reverseStr(n)
}

function digitToHex(t) {
    var e = 15
        , n = "";
    for (i = 0; 4 > i; ++i)
        n += hexToChar[t & e],
            t >>>= 4;
    return reverseStr(n)
}

function biToHex(t) {
    for (var e = "", i = (biHighIndex(t),
        biHighIndex(t)); i > -1; --i)
        e += digitToHex(t.digits[i]);
    return e
}

function charToHex(t) {
    var e, i = 48, n = i + 9, a = 97, o = a + 25, s = 65, r = 90;
    return e = t >= i && n >= t ? t - i : t >= s && r >= t ? 10 + t - s : t >= a && o >= t ? 10 + t - a : 0
}

function hexToDigit(t) {
    for (var e = 0, i = Math.min(t.length, 4), n = 0; i > n; ++n)
        e <<= 4,
            e |= charToHex(t.charCodeAt(n));
    return e
}

function biFromHex(t) {
    for (var e = new BigInt, i = t.length, n = i, a = 0; n > 0; n -= 4,
        ++a)
        e.digits[a] = hexToDigit(t.substr(Math.max(n - 4, 0), Math.min(n, 4)));
    return e
}

function biFromString(t, e) {
    var i = "-" == t.charAt(0)
        , n = i ? 1 : 0
        , a = new BigInt
        , o = new BigInt;
    o.digits[0] = 1;
    for (var s = t.length - 1; s >= n; s--) {
        var r = t.charCodeAt(s)
            , c = charToHex(r)
            , l = biMultiplyDigit(o, c);
        a = biAdd(a, l),
            o = biMultiplyDigit(o, e)
    }
    return a.isNeg = i,
        a
}

function biDump(t) {
    return (t.isNeg ? "-" : "") + t.digits.join(" ")
}

function biAdd(t, e) {
    var i;
    if (t.isNeg != e.isNeg)
        e.isNeg = !e.isNeg,
            i = biSubtract(t, e),
            e.isNeg = !e.isNeg;
    else {
        i = new BigInt;
        for (var n, a = 0, o = 0; o < t.digits.length; ++o)
            n = t.digits[o] + e.digits[o] + a,
                i.digits[o] = 65535 & n,
                a = Number(n >= biRadix);
        i.isNeg = t.isNeg
    }
    return i
}

function biSubtract(t, e) {
    var i;
    if (t.isNeg != e.isNeg)
        e.isNeg = !e.isNeg,
            i = biAdd(t, e),
            e.isNeg = !e.isNeg;
    else {
        i = new BigInt;
        var n, a;
        a = 0;
        for (var o = 0; o < t.digits.length; ++o)
            n = t.digits[o] - e.digits[o] + a,
                i.digits[o] = 65535 & n,
            i.digits[o] < 0 && (i.digits[o] += biRadix),
                a = 0 - Number(0 > n);
        if (-1 == a) {
            a = 0;
            for (var o = 0; o < t.digits.length; ++o)
                n = 0 - i.digits[o] + a,
                    i.digits[o] = 65535 & n,
                i.digits[o] < 0 && (i.digits[o] += biRadix),
                    a = 0 - Number(0 > n);
            i.isNeg = !t.isNeg
        } else
            i.isNeg = t.isNeg
    }
    return i
}

function biHighIndex(t) {
    for (var e = t.digits.length - 1; e > 0 && 0 == t.digits[e];)
        --e;
    return e
}

function biNumBits(t) {
    var e, i = biHighIndex(t), n = t.digits[i], a = (i + 1) * bitsPerDigit;
    for (e = a; e > a - bitsPerDigit && 0 == (32768 & n); --e)
        n <<= 1;
    return e
}

function biMultiply(t, e) {
    for (var i, n, a, o = new BigInt, s = biHighIndex(t), r = biHighIndex(e), c = 0; r >= c; ++c) {
        for (i = 0,
                 a = c,
                 j = 0; s >= j; ++j,
                 ++a)
            n = o.digits[a] + t.digits[j] * e.digits[c] + i,
                o.digits[a] = n & maxDigitVal,
                i = n >>> biRadixBits;
        o.digits[c + s + 1] = i
    }
    return o.isNeg = t.isNeg != e.isNeg,
        o
}

function biMultiplyDigit(t, e) {
    var i, n, a;
    result = new BigInt,
        i = biHighIndex(t),
        n = 0;
    for (var o = 0; i >= o; ++o)
        a = result.digits[o] + t.digits[o] * e + n,
            result.digits[o] = a & maxDigitVal,
            n = a >>> biRadixBits;
    return result.digits[1 + i] = n,
        result
}

function arrayCopy(t, e, i, n, a) {
    for (var o = Math.min(e + a, t.length), s = e, r = n; o > s; ++s,
        ++r)
        i[r] = t[s]
}

function biShiftLeft(t, e) {
    var i = Math.floor(e / bitsPerDigit)
        , n = new BigInt;
    arrayCopy(t.digits, 0, n.digits, i, n.digits.length - i);
    for (var a = e % bitsPerDigit, o = bitsPerDigit - a, s = n.digits.length - 1, r = s - 1; s > 0; --s,
        --r)
        n.digits[s] = n.digits[s] << a & maxDigitVal | (n.digits[r] & highBitMasks[a]) >>> o;
    return n.digits[0] = n.digits[s] << a & maxDigitVal,
        n.isNeg = t.isNeg,
        n
}

function biShiftRight(t, e) {
    var i = Math.floor(e / bitsPerDigit)
        , n = new BigInt;
    arrayCopy(t.digits, i, n.digits, 0, t.digits.length - i);
    for (var a = e % bitsPerDigit, o = bitsPerDigit - a, s = 0, r = s + 1; s < n.digits.length - 1; ++s,
        ++r)
        n.digits[s] = n.digits[s] >>> a | (n.digits[r] & lowBitMasks[a]) << o;
    return n.digits[n.digits.length - 1] >>>= a,
        n.isNeg = t.isNeg,
        n
}

function biMultiplyByRadixPower(t, e) {
    var i = new BigInt;
    return arrayCopy(t.digits, 0, i.digits, e, i.digits.length - e),
        i
}

function biDivideByRadixPower(t, e) {
    var i = new BigInt;
    return arrayCopy(t.digits, e, i.digits, 0, i.digits.length - e),
        i
}

function biModuloByRadixPower(t, e) {
    var i = new BigInt;
    return arrayCopy(t.digits, 0, i.digits, 0, e),
        i
}

function biCompare(t, e) {
    if (t.isNeg != e.isNeg)
        return 1 - 2 * Number(t.isNeg);
    for (var i = t.digits.length - 1; i >= 0; --i)
        if (t.digits[i] != e.digits[i])
            return t.isNeg ? 1 - 2 * Number(t.digits[i] > e.digits[i]) : 1 - 2 * Number(t.digits[i] < e.digits[i]);
    return 0
}

function biDivideModulo(t, e) {
    var i, n, a = biNumBits(t), o = biNumBits(e), s = e.isNeg;
    if (o > a)
        return t.isNeg ? (i = biCopy(bigOne),
            i.isNeg = !e.isNeg,
            t.isNeg = !1,
            e.isNeg = !1,
            n = biSubtract(e, t),
            t.isNeg = !0,
            e.isNeg = s) : (i = new BigInt,
            n = biCopy(t)),
            new Array(i, n);
    i = new BigInt,
        n = t;
    for (var r = Math.ceil(o / bitsPerDigit) - 1, c = 0; e.digits[r] < biHalfRadix;)
        e = biShiftLeft(e, 1),
            ++c,
            ++o,
            r = Math.ceil(o / bitsPerDigit) - 1;
    n = biShiftLeft(n, c),
        a += c;
    for (var l = Math.ceil(a / bitsPerDigit) - 1, u = biMultiplyByRadixPower(e, l - r); -1 != biCompare(n, u);)
        ++i.digits[l - r],
            n = biSubtract(n, u);
    for (var d = l; d > r; --d) {
        var p = d >= n.digits.length ? 0 : n.digits[d]
            , f = d - 1 >= n.digits.length ? 0 : n.digits[d - 1]
            , h = d - 2 >= n.digits.length ? 0 : n.digits[d - 2]
            , g = r >= e.digits.length ? 0 : e.digits[r]
            , m = r - 1 >= e.digits.length ? 0 : e.digits[r - 1];
        i.digits[d - r - 1] = p == g ? maxDigitVal : Math.floor((p * biRadix + f) / g);
        for (var b = i.digits[d - r - 1] * (g * biRadix + m), v = p * biRadixSquared + (f * biRadix + h); b > v;)
            --i.digits[d - r - 1],
                b = i.digits[d - r - 1] * (g * biRadix | m),
                v = p * biRadix * biRadix + (f * biRadix + h);
        u = biMultiplyByRadixPower(e, d - r - 1),
            n = biSubtract(n, biMultiplyDigit(u, i.digits[d - r - 1])),
        n.isNeg && (n = biAdd(n, u),
            --i.digits[d - r - 1])
    }
    return n = biShiftRight(n, c),
        i.isNeg = t.isNeg != s,
    t.isNeg && (i = s ? biAdd(i, bigOne) : biSubtract(i, bigOne),
        e = biShiftRight(e, c),
        n = biSubtract(e, n)),
    0 == n.digits[0] && 0 == biHighIndex(n) && (n.isNeg = !1),
        new Array(i, n)
}

function biDivide(t, e) {
    return biDivideModulo(t, e)[0]
}

function biModulo(t, e) {
    return biDivideModulo(t, e)[1]
}

function biMultiplyMod(t, e, i) {
    return biModulo(biMultiply(t, e), i)
}

function biPow(t, e) {
    for (var i = bigOne, n = t; 0 != (1 & e) && (i = biMultiply(i, n)),
        e >>= 1,
    0 != e;)
        n = biMultiply(n, n);
    return i
}

function biPowMod(t, e, i) {
    for (var n = bigOne, a = t, o = e; 0 != (1 & o.digits[0]) && (n = biMultiplyMod(n, a, i)),
        o = biShiftRight(o, 1),
    0 != o.digits[0] || 0 != biHighIndex(o);)
        a = biMultiplyMod(a, a, i);
    return n
}

function RSAKeyPair(t, e, i) {
    this.e = biFromHex(t),
        this.d = biFromHex(e),
        this.m = biFromHex(i),
        this.chunkSize = 2 * biHighIndex(this.m),
        this.radix = 16,
        this.barrett = new BarrettMu(this.m)
}

function twoDigit(t) {
    return (10 > t ? "0" : "") + String(t)
}

function encryptedString(t, e) {
    for (var i = new Array, n = e.length, a = 0; n > a;)
        i[a] = e.charCodeAt(a),
            a++;
    for (; i.length % t.chunkSize != 0;)
        i[a++] = 0;
    var o, s, r, c = i.length, l = "";
    for (a = 0; c > a; a += t.chunkSize) {
        for (r = new BigInt,
                 o = 0,
                 s = a; s < a + t.chunkSize; ++o)
            r.digits[o] = i[s++],
                r.digits[o] += i[s++] << 8;
        var u = t.barrett.powMod(r, t.e)
            , d = 16 == t.radix ? biToHex(u) : biToString(u, t.radix);
        l += d + " "
    }
    return l.substring(0, l.length - 1)
}

function decryptedString(t, e) {
    var i, n, a, o = e.split(" "), s = "";
    for (i = 0; i < o.length; ++i) {
        var r;
        for (r = 16 == t.radix ? biFromHex(o[i]) : biFromString(o[i], t.radix),
                 a = t.barrett.powMod(r, t.d),
                 n = 0; n <= biHighIndex(a); ++n)
            s += String.fromCharCode(255 & a.digits[n], a.digits[n] >> 8)
    }
    return 0 == s.charCodeAt(s.length - 1) && (s = s.substring(0, s.length - 1)),
        s
}

var biRadixBase = 2, biRadixBits = 16, bitsPerDigit = biRadixBits, biRadix = 65536, biHalfRadix = biRadix >>> 1, biRadixSquared = biRadix * biRadix, maxDigitVal = biRadix - 1, maxInteger = 9999999999999998, maxDigits, ZERO_ARRAY, bigZero, bigOne;
setMaxDigits(20);
var dpl10 = 15
    , lr10 = biFromNumber(1e15)
    , hexatrigesimalToChar = new Array("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z")
    , hexToChar = new Array("0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f")
    , highBitMasks = new Array(0,32768,49152,57344,61440,63488,64512,65024,65280,65408,65472,65504,65520,65528,65532,65534,65535)
    , lowBitMasks = new Array(0,1,3,7,15,31,63,127,255,511,1023,2047,4095,8191,16383,32767,65535);






