package main.java.commandparser;

public class ValidateCommands
{

    public static class ValidateIndexCommands
    {
        RegisterCommands.CommandIndex indexParser=null;
        public ValidateIndexCommands(RegisterCommands.CommandIndex indexParser)
        {
            this.indexParser = indexParser;
        }

        private void CALLEXIT(int status)
        {
            System.exit(status);
        }

        public void ValidateIndex()
        {
            if(indexParser.getIndexPath()==null)
            {
                CALLEXIT(-1);
            }
        }


    }


    public static class ValidateSearchCommands
    {

        private RegisterCommands.CommandSearch searchParser = null;

        public ValidateSearchCommands(RegisterCommands.CommandSearch searchParser)
        {
            this.searchParser = searchParser;
        }


        private void CALLEXIT(int status)
        {
            System.exit(status);
        }

        public void ValidateReRank()
        {
            if(searchParser.getWordEmbeddingFile()== null)
            {
                System.out.println("Please pass the word Embeddings file");
                CALLEXIT(-1);
            }

            if(searchParser.getDimension()==0)
            {
                System.out.println("Please pass the dimension of the word vectors");
                CALLEXIT(-1);
            }

        }

        public void ValidateRetrievalOptions()
        {
            if(!searchParser.isArticleEnabled() && !searchParser.isSectionEnabled())
            {
                System.out.println("Please use either article or section level option");
                CALLEXIT(-1);
            }
            else if (searchParser.isArticleEnabled() && searchParser.isSectionEnabled())
            {
                System.out.println("Please use either section level or article level option, not bot");
                CALLEXIT(-1);
            }
        }

        public void ValidateQE() {
            if (searchParser.getNumberOfReturnedEntity() == 0) {
                System.out.println("Please pass the number of entity to be returned");
                CALLEXIT(-1);
            }
        }

        public void ValidateEcmExpansion(){
            if(searchParser.getIndexlocation()== null)
            {
                System.out.println("Please pass the paragraph index location path");
                CALLEXIT(-1);
            }
            if(searchParser.getEcmentityfile() == null)
            {
                System.out.println("Please pass the entity run file path");
                CALLEXIT(-1);
            }
        }
        public void ValidateEntityDegree()
        {
            if(searchParser.getEntityIndLoc() == null)
            {
                System.out.println("Please pass the entity index location path");
                CALLEXIT(-1);
            }
        }

        public void ValidateEntityRelation()
        {
            if(searchParser.getEntityIndLoc() == null)
            {
                System.out.println("Please pass the entity index location path");
                CALLEXIT(-1);
            }
            if(searchParser.getQrelfile() == null)
            {
                System.out.println("Please pass the entity qrel location path");
                CALLEXIT(-1);
            }
            if(searchParser.getEcmentityfile() == null)
            {
                System.out.println("Please pass the entity run file path");
                CALLEXIT(-1);
            }

        }

        public void ValidateEntityRankLib()
        {
            if(searchParser.getFeaturevectorfile() == null)
            {
                System.out.println("Please pass the feature vector file path");
                CALLEXIT(-1);
            }
            if(searchParser.getRankLibModelFile() == null)
            {
                System.out.println("Please pass the entity ranklib model location path");
                CALLEXIT(-1);
            }

        }

        public void ValidateEntityCentroid()
        {
            if(searchParser.getFeaturevectorfile() == null)
            {
                System.out.println("Please pass the feature vector file path");
                CALLEXIT(-1);
            }
        }


        public void ValidateEntitySim() {
            if (searchParser.getWordEmbeddingFile() == null) {
                System.out.println("Please pass the word Embeddings file");
                CALLEXIT(-1);
            }

            if (searchParser.getDimension() == 0) {
                System.out.println("Please pass the dimension of the word vectors");
                CALLEXIT(-1);
            }

            if (searchParser.getEntityIndLoc() == null) {
                System.out.println("Please pass the entity index location path");
                CALLEXIT(-1);
            }

        }

    }


}
