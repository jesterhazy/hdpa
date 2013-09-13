datadir <- '/Users/jonathan/Desktop/data'

nytKappaFiles <- matrix(c(
'nyt-random-t5000-model-b500-k0.5-20130528-0558', 'coherence-20130529-2150.txt', 'eval-20130530-0022.txt',
'nyt-random-t5000-model-b500-k0.6-20130528-0559', 'coherence-20130529-2152.txt', 'eval-20130530-0030.txt',
'nyt-random-t5000-model-b500-k0.7-20130528-0611', 'coherence-20130530-0723.txt', 'eval-20130530-1001.txt',
'nyt-random-t5000-model-b500-k0.8-20130528-0614', 'coherence-20130530-0723.txt', 'eval-20130530-1025.txt',
'nyt-random-t5000-model-b500-k0.9-20130529-0546', 'coherence-20130531-0455.txt', 'eval-20130531-0600.txt',
'nyt-random-t5000-model-b500-k1.0-20130528-0621', 'coherence-20130531-1702.txt', 'eval-20130531-2002.txt'
), 6, 3, byrow=T)


nytBatchFiles <- matrix(c(
'nyt-random-t5000-model-b10-k0.9-20130528-0627', 'coherence-20130605-0831.txt', 'eval-20130601-1646.txt',
'nyt-random-t5000-model-b50-k0.9-20130528-0640', 'coherence-20130530-1948.txt', 'eval-20130531-1256.txt',
'nyt-random-t5000-model-b100-k0.9-20130528-0648', 'coherence-20130531-1706.txt', 'eval-20130601-0218.txt',
'nyt-random-t5000-model-b500-k0.9-20130529-0546', 'coherence-20130531-0455.txt', 'eval-20130531-0600.txt',
'nyt-random-t5000-model-b1000-k0.9-20130528-0653', 'coherence-20130530-2115.txt', 'eval-20130530-2324.txt'
), 5, 3, byrow=T)

nanKappaFiles <- matrix(c(
'nan-random-t5000-model-b500-k0.5-20130528-0654', 'coherence-20130529-0657.txt', 'eval-20130529-0840.txt',
'nan-random-t5000-model-b500-k0.6-20130528-0656', 'coherence-20130529-0709.txt', 'eval-20130529-0901.txt',
'nan-random-t5000-model-b500-k0.7-20130528-0656', 'coherence-20130529-0755.txt', 'eval-20130529-0959.txt',
'nan-random-t5000-model-b500-k0.8-20130528-0703', 'coherence-20130529-0803.txt', 'eval-20130529-1013.txt',
'nan-random-t5000-model-b500-k0.9-20130529-0546', 'coherence-20130530-1952.txt', 'eval-20130530-2108.txt',
'nan-random-t5000-model-b500-k1.0-20130528-0703', 'coherence-20130529-1559.txt', 'eval-20130529-1807.txt'
), 6, 3, byrow=T)

nanBatchFiles <- matrix(c(
'nan-random-t5000-model-b10-k0.9-20130528-0703', 'coherence-20130531-0456.txt', 'eval-20130601-1256.txt',
'nan-random-t5000-model-b50-k0.9-20130528-0907', 'coherence-20130529-2150.txt', 'eval-20130530-0551.txt',
'nan-random-t5000-model-b100-k0.9-20130528-0907', 'coherence-20130529-2149.txt', 'eval-20130530-0258.txt',
'nan-random-t5000-model-b500-k0.9-20130529-0546', 'coherence-20130530-1952.txt', 'eval-20130530-2108.txt',
'nan-random-t5000-model-b1000-k0.9-20130528-0808', 'coherence-20130529-1044.txt', 'eval-20130529-1229.txt'
), 5, 3, byrow=T)


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

createLikelihoodChart <- function(targetdir, title, data, labels) {
	chartfile <- tolower(gsub("$", ".pdf", gsub(" ", "-", title)))
	pdf(paste(targetdir, chartfile, sep='/'), width=4, height=4)

	perwords <- lapply(data, function(x) x$per.word)
	totaldocs <- lapply(data, function(x) x$total.docs)

	xrange <- range(0, round(max(unlist(totaldocs)), -5))
	yrange <- range(min(unlist(perwords)), max(unlist(perwords)))

	#par(oma=c(0,0,0,0), mar=c(3,3,2,2)+0.1, cex=0.8)
	par(oma=c(0,0,0,0), mar=c(4,4,2,2)+0.1, cex=0.8)
	plot(xrange, yrange, xaxt="n", type="n", xlab="documents analyzed", ylab="per-word log likelihood")
	aty = seq(from=0, to=xrange[2], length.out=5)
	axis(1, at=aty, labels=formatC(aty, format="d"))

	ltys = c(1:length(labels))
	lapply(ltys, function(x) lines(data[[x]]$total.docs, data[[x]]$per.word, type="l", lwd="1.5", lty=x))

	legend("bottomright", legend=labels, inset=0.1, cex=0.8, lty=ltys)
	dev.off()
}

createCoherenceChart <- function(targetdir, title, data, labels) {
	chartfile <- tolower(gsub("$", ".pdf", gsub(" ", "-", title)))
	pdf(paste(targetdir, chartfile, sep='/'), width=4, height=4)

	coherence <- lapply(data, function(x) x$coherence)

	xrange <- range(0, 300)
	yrange <- range(min(unlist(coherence)), max(unlist(coherence)))

	#par(oma=c(0,0,0,0), mar=c(3,3,2,2)+0.1, cex=0.8)
	par(oma=c(0,0,0,0), mar=c(4,4,2,2)+0.1, cex=0.8)
	plot(xrange, yrange, xaxt="n", type="n", xlab="topic index", ylab="coherence")
	aty = seq(from=0, to=xrange[2], length.out=5)
	axis(1, at=aty, labels=formatC(aty, format="d"))
	axis(2)

	ltys = c(1:length(labels))
	lapply(ltys, function(x) lines(sort(data[[x]]$coherence, T), type="l", lwd="1.5", lty=x))

	legend("bottomleft", legend=labels, inset=0.1, cex=0.8, lty=ltys)
	dev.off()
}

labels <- function(filenames, prefix) {
	pattern = paste(".*-([", prefix, "][0-9.]+)-.*")
	lapply(filenames, function(x) gsub(pattern, "\\1", x))
}

likelihoodData = apply(nytKappaFiles, 1, readLikelihood)
createLikelihoodChart('/Users/jonathan/Desktop', 'NYT Kappa Likelihood', likelihoodData, labels(nytKappaFiles[,1], "k"))

likelihoodData = apply(nanKappaFiles, 1, readLikelihood)
createLikelihoodChart('/Users/jonathan/Desktop', 'NAN Kappa Likelihood', likelihoodData, labels(nanKappaFiles[,1], "k"))

likelihoodData = apply(nytBatchFiles, 1, readLikelihood)
createLikelihoodChart('/Users/jonathan/Desktop', 'NYT Batch Size Likelihood', likelihoodData, labels(nytBatchFiles[,1], "b"))

likelihoodData = apply(nanBatchFiles, 1, readLikelihood)
createLikelihoodChart('/Users/jonathan/Desktop', 'NAN Batch Size Likelihood', likelihoodData, labels(nanBatchFiles[,1], "b"))

coherenceData = apply(nytKappaFiles, 1, readCoherence)
createCoherenceChart('/Users/jonathan/Desktop', 'NYT Kappa Coherence', coherenceData, labels(nytKappaFiles[,1], "k"))

coherenceData = apply(nanKappaFiles, 1, readCoherence)
createCoherenceChart('/Users/jonathan/Desktop', 'NAN Kappa Coherence', coherenceData, labels(nanKappaFiles[,1], "k"))

coherenceData = apply(nytBatchFiles, 1, readCoherence)
createCoherenceChart('/Users/jonathan/Desktop', 'NYT Batch Size Coherence', coherenceData, labels(nytBatchFiles[,1], "b"))

coherenceData = apply(nanBatchFiles, 1, readCoherence)
createCoherenceChart('/Users/jonathan/Desktop', 'NAN Batch Size Coherence', coherenceData, labels(nanBatchFiles[,1], "b"))
