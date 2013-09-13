hdpaDir <- '/Users/jonathan/Desktop/data'
hdpaFiles <- matrix(c(
  hdpaDir, 'nan-random-t5000-model-b500-k0.9-20130909-0819', 'coherence-20130911-0735.txt', 'eval-20130911-0939.txt',
  hdpaDir, 'nyt-random-t5000-model-b500-k0.9-20130909-0817', 'coherence-20130912-1425.txt', 'eval-20130912-0853.txt'
), 2, 4, byrow=T)

ohdpDir <- '/Users/jonathan/Documents/6 uathabasca/project/698 - implementation/wang comparison/dat/20130331 complete runs'
ohdpFiles <- matrix(c(
  ohdpDir, 'corpus-nan-kappa-0.9-tau-1-batchsize-500', 'coherence-20130605-1523.txt', 'test-log.dat', 13,
  ohdpDir, 'corpus-nyt-kappa-0.9-tau-1-batchsize-500', 'coherence-20130605-1506.txt', 'test-log.dat', 14
), 2, 5, byrow=T)

readHdpa <- function(ff) {
	f <- readLines(paste(ff[1], ff[2], ff[4], sep='/'))
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub("  +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

readOhdp <- function(ff) {
	f <- readLines(paste(ff[1], ff[2], ff[4], sep='/'))
	f <- f[1:ff[5]]
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub(" +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

readCoherenceFile <- function(ff) {
	f <- readLines(paste(ff[1], ff[2], ff[3], sep='/'))
	f <- f[c(1, 3:302)]
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub("'", "", f)
	f <- gsub("  +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

createLikelihoodChart <- function(targetdir, title, hdpa, ohdp) {
	chartfile <- tolower(gsub("$", ".pdf", gsub(" ", "-", title)))
	pdf(paste(targetdir, chartfile, sep='/'), width=4, height=4)

	ohdp$doc.count[length(ohdp$doc.count)] = hdpa$total.docs[length(hdpa$total.docs)]
	ohdpPerword <- apply(as.matrix(ohdp$score), 1, function(x) x / ohdp$word.count[1])

	xrange <- range(0, round(max(hdpa$total.docs, ohdp$doc.count), -5))
	yrange <- range(min(hdpa$per.word, ohdpPerword), max(hdpa$per.word, ohdpPerword))

	par(oma=c(0,0,0,0), mar=c(4,4,2,2)+0.1, cex=0.8)
	plot(xrange, yrange, xaxt="n", type="n", xlab="documents analyzed", ylab="per-word log likelihood")
	aty = seq(from=xrange[1], to=xrange[2], length.out=3)
	axis(1, at=aty, labels=formatC(aty, format="d"))

	lines = c(1,2)
	lines(hdpa$total.docs, hdpa$per.word, type="l", lwd=1.5, lty=lines[1], pch=18)
	lines(ohdp$doc.count, ohdpPerword, type="l", lwd=1.5, lty=lines[2], pch=18)

	legend("bottomright", c('hdpa', 'ohdp'), inset=0.1, cex=0.8, pch=18, lty=lines)
	dev.off()
}

createCoherenceChart <- function(targetdir, title, hdpa, ohdp) {
	chartfile <- tolower(gsub("$", ".pdf", gsub(" ", "-", title)))
	pdf(paste(targetdir, chartfile, sep='/'), width=4, height=4)

	xrange <- range(0, 300)
	yrange <- range(min(hdpa$coherence, ohdp$coherence), max(hdpa$coherence, ohdp$coherence))

	par(oma=c(0,0,0,0), mar=c(4,4,2,2)+0.1, cex=0.8)
	plot(xrange, yrange, xaxt="n", type="n", xlab="topic index", ylab="coherence")
	aty = seq(from=xrange[1], to=xrange[2], length.out=3)
	axis(1, at=aty, labels=formatC(aty, format="d"))
	axis(2)

	lines = c(1,2)
	lines(sort(hdpa$coherence, T), type="l", lwd=1.5, lty=lines[1], pch=18)
	lines(sort(ohdp$coherence, T), type="l", lwd=1.5, lty=lines[2], pch=18)

	legend("bottomleft", c('hdpa', 'ohdp'), inset=0.1, cex=0.8, pch=18, lty=lines)
	dev.off()
}

hdpa = readHdpa(hdpaFiles[1,])
ohdp = readOhdp(ohdpFiles[1,])
createLikelihoodChart('/Users/jonathan/Desktop', 'NAN Likelihood', hdpa, ohdp)

hdpa = readHdpa(hdpaFiles[2,])
ohdp = readOhdp(ohdpFiles[2,])
createLikelihoodChart('/Users/jonathan/Desktop', 'NYT Likelihood', hdpa, ohdp)

hdpa = readCoherenceFile(hdpaFiles[1,])
ohdp = readCoherenceFile(ohdpFiles[1,])
createCoherenceChart('/Users/jonathan/Desktop', 'NAN Coherence', hdpa, ohdp)

hdpa = readCoherenceFile(hdpaFiles[2,])
ohdp = readCoherenceFile(ohdpFiles[2,])
createCoherenceChart('/Users/jonathan/Desktop', 'NYT Coherence', hdpa, ohdp)
