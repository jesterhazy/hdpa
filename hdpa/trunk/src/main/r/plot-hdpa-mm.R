datadir <- '/Users/jonathan/Desktop/data'

files <- matrix(c(
	'nyt-random-t5000-model-b500-k0.9-20130529-0546', 'coherence-20130531-0455.txt', 'eval-20130531-0756.txt',
	'nan-random-t5000-model-b500-k0.9-20130529-0546', 'coherence-20130530-1952.txt', 'eval-20130530-2209.txt'
), 2, 3, byrow=T)


readLikelihood <- function(ff) {
	f <- readLines(paste(datadir, ff[1], ff[3], sep='/'))
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub("  +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

readCoherence <- function(ff) {
	f <- readLines(paste(datadir, ff[1], ff[2], sep='/'))
	f <- f[c(1, 3:302)]
	f <- gsub("^ *(.*) *$", "\\1", f)
	f <- gsub("'", "", f)
	f <- gsub("  +", "\t", f)
	tc <- textConnection(f)
	d = read.table(tc, header=TRUE, sep="\t")
	return (d)
}

createLikelihoodChart <- function(targetdir, title, data) {
	chartfile <- tolower(gsub("$", ".pdf", gsub(" ", "-", title)))
	pdf(paste(targetdir, chartfile, sep='/'), width=4, height=4)

	xrange <- range(0, round(max(data$total.docs), -5))
	yrange <- range(min(data$per.word), max(data$per.word))

	par(oma=c(0,0,0,0), mar=c(4,4,2,2)+0.1, cex=0.8)
	plot(xrange, yrange, xaxt="n", type="n", xlab="documents analyzed", ylab="per-word log likelihood")
	aty = seq(from=0, to=xrange[2], length.out=5)
	axis(1, at=aty, labels=formatC(aty, format="d"))

	lines(data$total.docs, data$per.word, type="l", lwd="1.5", lty=1)
	dev.off()
}

createCoherenceChart <- function(targetdir, title, data) {
	chartfile <- tolower(gsub("$", ".pdf", gsub(" ", "-", title)))
	pdf(paste(targetdir, chartfile, sep='/'), width=4, height=4)

	xrange <- range(0, 300)
	yrange <- range(min(data$coherence), max(data$coherence))

	par(oma=c(0,0,0,0), mar=c(4,4,2,2)+0.1, cex=0.8)
	plot(xrange, yrange, xaxt="n", type="n", xlab="topic index", ylab="coherence")
	aty = seq(from=0, to=xrange[2], length.out=5)
	axis(1, at=aty, labels=formatC(aty, format="d"))
	axis(2)

    lines(sort(data$coherence, T), type="l", lwd="1.5", lty=1)
	dev.off()
}

likelihoodData = readLikelihood(files[1,])
createLikelihoodChart('/Users/jonathan/Desktop', 'NYT MM Likelihood', likelihoodData)

likelihoodData = readLikelihood(files[2,])
createLikelihoodChart('/Users/jonathan/Desktop', 'NAN MM Likelihood', likelihoodData)

coherenceData = readCoherence(files[1,])
createCoherenceChart('/Users/jonathan/Desktop', 'NYT MM Coherence', coherenceData)

coherenceData = readCoherence(files[2,])
createCoherenceChart('/Users/jonathan/Desktop', 'NAN MM Coherence', coherenceData)

